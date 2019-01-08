package ru.saidgadjiev.bibliography.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.saidgadjiev.bibliography.dao.api.BiographyDao;
import ru.saidgadjiev.bibliography.dao.impl.firebase.FirebaseBiographyDao;
import ru.saidgadjiev.bibliography.data.FilterArgumentResolver;
import ru.saidgadjiev.bibliography.data.FilterCriteria;
import ru.saidgadjiev.bibliography.data.FilterOperation;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.BiographyUpdateStatus;
import ru.saidgadjiev.bibliography.domain.User;
import ru.saidgadjiev.bibliography.model.BiographyRequest;
import ru.saidgadjiev.bibliography.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliography.model.firebase.BiographyStats;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by said on 22.10.2018.
 */
@Service
public class BiographyService {

    private final BiographyDao biographyDao;

    private final BiographyDao firebaseBiographyDao;

    private final SecurityService securityService;

    private final FilterArgumentResolver argumentResolver;

    private final BiographyLikeService biographyLikeService;

    private final BiographyCommentService biographyCommentService;

    private final BiographyCategoryBiographyService biographyCategoryBiographyService;

    @Autowired
    public BiographyService(@Qualifier("sql") BiographyDao biographyDao,
                            @Qualifier("firebase") BiographyDao firebaseBiographyDao,
                            SecurityService securityService,
                            FilterArgumentResolver argumentResolver,
                            BiographyLikeService biographyLikeService,
                            BiographyCommentService biographyCommentService,
                            BiographyCategoryBiographyService biographyCategoryBiographyService) {
        this.biographyDao = biographyDao;
        this.firebaseBiographyDao = firebaseBiographyDao;
        this.securityService = securityService;
        this.argumentResolver = argumentResolver;
        this.biographyLikeService = biographyLikeService;
        this.biographyCommentService = biographyCommentService;
        this.biographyCategoryBiographyService = biographyCategoryBiographyService;
    }

    @Transactional
    public Biography create(BiographyRequest biographyRequest) throws SQLException {
        User userDetails = (User) securityService.findLoggedInUser();

        Biography biography = new Biography();

        biography.setFirstName(biographyRequest.getFirstName());
        biography.setLastName(biographyRequest.getLastName());
        biography.setMiddleName(biographyRequest.getMiddleName());
        biography.setBiography(biographyRequest.getBiography());
        biography.setCreatorId(userDetails.getId());
        biography.setUserId(biographyRequest.getUserId());

        Biography result = biographyDao.save(biography);

        biographyCategoryBiographyService.addCategoriesToBiography(
                biographyRequest.getAddCategories(),
                result.getId()
        );

        result.setCategories(biographyRequest.getAddCategories());

        firebaseBiographyDao.save(result);

        return result;
    }

    public Biography createAccountBiography(User user, BiographyRequest biographyRequest) throws SQLException {
        Biography biography = new Biography();

        biography.setFirstName(biographyRequest.getFirstName());
        biography.setLastName(biographyRequest.getLastName());
        biography.setMiddleName(biographyRequest.getMiddleName());
        biography.setBiography(biographyRequest.getBiography());
        biography.setCreatorId(user.getId());
        biography.setUserId(user.getId());

        return biographyDao.save(biography);
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

    public Page<Biography> getBiographies(OffsetLimitPageRequest pageRequest, String categoryName) {
        return getBiographies(pageRequest, Collections.emptyList(), categoryName);
    }


    public Page<Biography> getBiographies(OffsetLimitPageRequest pageRequest,
                                          Collection<FilterCriteria> criteria,
                                          String categoryName
    ) {
        List<Biography> biographies = biographyDao.getBiographiesList(
                pageRequest.getPageSize(), pageRequest.getOffset(), categoryName, criteria, pageRequest.getSort());

        if (biographies.isEmpty()) {
            return new PageImpl<>(biographies, pageRequest, 0);
        }

        postProcess(biographies);

        long total = biographyDao.countOff();

        return new PageImpl<>(biographies, pageRequest, total);
    }


    public Page<Biography> getMyBiographies(OffsetLimitPageRequest pageRequest) {
        User user = (User) securityService.findLoggedInUser();
        List<FilterCriteria> criteria = new ArrayList<>();

        criteria.add(
                new FilterCriteria<Integer>(
                        "creator_id",
                        FilterOperation.EQ,
                        PreparedStatement::setInt,
                        user.getId(),
                        true
                )
        );

        List<Biography> biographies = biographyDao.getBiographiesList(
                pageRequest.getPageSize(), pageRequest.getOffset(), null, criteria,
                null);

        if (biographies.isEmpty()) {
            return new PageImpl<>(biographies, pageRequest, 0);
        }

        postProcess(biographies);

        long total = biographyDao.countOff();

        return new PageImpl<>(biographies, pageRequest, total);
    }

    @Transactional
    public BiographyUpdateStatus update(Integer id, BiographyRequest updateBiographyRequest) throws SQLException {
        Biography biography = new Biography();

        biography.setFirstName(updateBiographyRequest.getFirstName());
        biography.setLastName(updateBiographyRequest.getLastName());
        biography.setMiddleName(updateBiographyRequest.getMiddleName());

        biography.setId(id);
        biography.setBiography(updateBiographyRequest.getBiography());

        Timestamp timestamp = new Timestamp(updateBiographyRequest.getLastModified().getTime());

        timestamp.setNanos(updateBiographyRequest.getLastModified().getNanos());

        biography.setUpdatedAt(timestamp);

        BiographyUpdateStatus status = biographyDao.update(biography);

        if (status.isUpdated()) {
            biographyCategoryBiographyService.addCategoriesToBiography(
                    updateBiographyRequest.getAddCategories(),
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
        BiographyStats stats = ((FirebaseBiographyDao) firebaseBiographyDao).getBiographyStats(biography.getId());

        biography.setLikesCount(stats.getLikesCount());
        biography.setCommentsCount(stats.getCommentsCount());
        biography.setLiked(biographyLikeService.getBiographyIsLiked(biography.getId()));
        biography.setCategories(biographyCategoryBiographyService.getBiographyCategories(biography.getId()));
    }

    private void postProcess(Collection<Biography> biographies) {
        List<Integer> ids = biographies.stream().map(Biography::getId).collect(Collectors.toList());
        Map<Integer, Boolean> biographiesIsLiked = biographyLikeService.getBiographiesIsLiked(ids);
        Map<Integer, BiographyStats> biographyStatsMap = ((FirebaseBiographyDao) firebaseBiographyDao).getBiographiesStats(ids);
        Map<Integer, Collection<String>> biographiesCategories = biographyCategoryBiographyService.getBiographiesCategories(ids);

        for (Biography biography : biographies) {
            biography.setLiked(biographiesIsLiked.get(biography.getId()));
            biography.setLikesCount(biographyStatsMap.get(biography.getId()).getLikesCount());
            biography.setCommentsCount(biographyStatsMap.get(biography.getId()).getCommentsCount());
            biography.setCategories(biographiesCategories.get(biography.getId()));
        }
    }
}
