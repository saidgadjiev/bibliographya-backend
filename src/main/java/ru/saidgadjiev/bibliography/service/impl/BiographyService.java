package ru.saidgadjiev.bibliography.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.saidgadjiev.bibliography.dao.BiographyDao;
import ru.saidgadjiev.bibliography.data.FilterArgumentResolver;
import ru.saidgadjiev.bibliography.data.FilterCriteria;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.BiographyUpdateStatus;
import ru.saidgadjiev.bibliography.model.BiographyRequest;
import ru.saidgadjiev.bibliography.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliography.security.service.SecurityService;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by said on 22.10.2018.
 */
@Service
public class BiographyService {

    private final BiographyDao biographyDao;

    private final SecurityService securityService;

    private final FilterArgumentResolver argumentResolver;

    private final BiographyLikeService biographyLikeService;

    private final BiographyCommentService biographyCommentService;

    private final BiographyCategoryBiographyService biographyCategoryBiographyService;

    @Autowired
    public BiographyService(BiographyDao biographyDao,
                            SecurityService securityService,
                            FilterArgumentResolver argumentResolver,
                            BiographyLikeService biographyLikeService,
                            BiographyCommentService biographyCommentService,
                            BiographyCategoryBiographyService biographyCategoryBiographyService) {
        this.biographyDao = biographyDao;
        this.securityService = securityService;
        this.argumentResolver = argumentResolver;
        this.biographyLikeService = biographyLikeService;
        this.biographyCommentService = biographyCommentService;
        this.biographyCategoryBiographyService = biographyCategoryBiographyService;
    }

    @Transactional
    public Biography create(BiographyRequest biographyRequest) throws SQLException {
        UserDetails userDetails = securityService.findLoggedInUser();

        Biography biography = new Biography.Builder(
                biographyRequest.getFirstName(),
                biographyRequest.getLastName(),
                biographyRequest.getMiddleName()
        )
                .setBiography(biographyRequest.getBiography())
                .setCreatorName(userDetails.getUsername())
                .setUserName(biographyRequest.getUserName())
                .build();

        Biography result = biographyDao.save(biography);

        biographyCategoryBiographyService.addCategoriesToBiography(
                biographyRequest.getAddedCategories(),
                result.getId()
        );

        result.setCategories(biographyRequest.getAddedCategories());

        return result;
    }

    public Biography getBiography(String userNameFilter) {
        List<FilterCriteria> criteria = new ArrayList<>();

        if (userNameFilter != null) {
            criteria.add(
                    argumentResolver.resolve(
                            "user_name",
                            String::valueOf,
                            PreparedStatement::setString,
                            userNameFilter
                    )
            );
        }

        Biography biography = biographyDao.getBiography(criteria);

        if (biography == null) {
            return null;
        }

        postProcess(biography);

        return biography;
    }

    public Biography getBiographyById(int id) {
        Biography biography = biographyDao.getById(id);

        if (biography == null) {
            return null;
        }

        postProcess(biography);

        return biography;
    }

    public Page<Biography> getBiographies(OffsetLimitPageRequest pageRequest,
                                          String creatorNameFilter,
                                          String moderationStatusFilter,
                                          String categoryName) {
        List<FilterCriteria> criteria = new ArrayList<>();

        if (creatorNameFilter != null) {
            criteria.add(
                    argumentResolver.resolve(
                            "creator_name",
                            String::valueOf, PreparedStatement::setString,
                            creatorNameFilter
                    )
            );
        }
        if (moderationStatusFilter != null) {
            criteria.add(
                    argumentResolver.resolve(
                            Biography.MODERATION_STATUS,
                            String::valueOf,
                            PreparedStatement::setString,
                            moderationStatusFilter
                    )
            );
        }

        List<Biography> biographies = biographyDao.getBiographiesList(
                pageRequest.getPageSize(), pageRequest.getOffset(), criteria, categoryName
        );

        if (biographies.isEmpty()) {
            return new PageImpl<>(biographies, pageRequest, 0);
        }

        postProcess(biographies);

        long total = biographyDao.countOff();

        return new PageImpl<>(biographies, pageRequest, total);
    }

    @Transactional
    public BiographyUpdateStatus update(Integer id, BiographyRequest updateBiographyRequest) throws SQLException {
        Biography.Builder builder = new Biography.Builder(
                updateBiographyRequest.getFirstName(),
                updateBiographyRequest.getLastName(),
                updateBiographyRequest.getMiddleName()
        );

        builder.setId(id);
        builder.setBiography(updateBiographyRequest.getBiography());

        Timestamp timestamp = new Timestamp(updateBiographyRequest.getLastModified().getTime());

        timestamp.setNanos(updateBiographyRequest.getLastModified().getNanos());

        builder.setUpdatedAt(timestamp);

        BiographyUpdateStatus status = biographyDao.update(builder.build());

        if (status.isUpdated()) {
            biographyCategoryBiographyService.addCategoriesToBiography(
                    updateBiographyRequest.getAddedCategories(),
                    id
            );
            biographyCategoryBiographyService.deleteCategoriesFromBiography(
                    updateBiographyRequest.getDeleteCategories(),
                    id
            );
        }

        return status;
    }

    private void postProcess(Biography biography) {
        biography.setLikesCount(biographyLikeService.getBiographyLikesCount(biography.getId()));
        biography.setCommentsCount(biographyCommentService.getBiographyCommentsCount(biography.getId()));
        biography.setLiked(biographyLikeService.getBiographyIsLiked(biography.getId()));
        biography.setCategories(biographyCategoryBiographyService.getBiographyCategories(biography.getId()));
    }

    private void postProcess(Collection<Biography> biographies) {
        Collection<Integer> ids = biographies.stream().map(Biography::getId).collect(Collectors.toList());
        Map<Integer, Integer> biographiesLikesCount = biographyLikeService.getBiographiesLikesCount(ids);
        Map<Integer, Boolean> biographiesIsLiked = biographyLikeService.getBiographiesIsLiked(ids);
        Map<Integer, Long> biographiesCommentsCount = biographyCommentService.getBiographiesCommentsCount(ids);
        Map<Integer, Collection<String>> biographiesCategories = biographyCategoryBiographyService.getBiographiesCategories(ids);

        for (Biography biography: biographies) {
            biography.setLikesCount(biographiesLikesCount.get(biography.getId()));
            biography.setLiked(biographiesIsLiked.get(biography.getId()));
            biography.setCommentsCount(biographiesCommentsCount.get(biography.getId()));
            biography.setCategories(biographiesCategories.get(biography.getId()));
        }
    }
}
