package ru.saidgadjiev.bibliography.service.impl;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.saidgadjiev.bibliography.dao.api.BiographyDao;
import ru.saidgadjiev.bibliography.data.*;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.BiographyUpdateStatus;
import ru.saidgadjiev.bibliography.domain.User;
import ru.saidgadjiev.bibliography.model.BiographyRequest;
import ru.saidgadjiev.bibliography.model.OffsetLimitPageRequest;

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

    private final SecurityService securityService;

    private final FilterArgumentResolver argumentResolver;

    private final BiographyLikeService biographyLikeService;

    private final BiographyCommentService biographyCommentService;

    private final BiographyCategoryBiographyService biographyCategoryBiographyService;

    @Autowired
    public BiographyService(@Qualifier("sql") BiographyDao biographyDao,
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
        User userDetails = (User) securityService.findLoggedInUser();

        Biography biography = new Biography();

        biography.setFirstName(biographyRequest.getFirstName());
        biography.setLastName(biographyRequest.getLastName());
        biography.setMiddleName(biographyRequest.getMiddleName());
        biography.setBiography(biographyRequest.getBiography());
        biography.setCreatorId(userDetails.getId());
        biography.setUserId(biographyRequest.getUserId());

        if (Objects.equals(biography.getCreatorId(), biography.getUserId())) {
            biography.setPublishStatus(Biography.PublishStatus.PUBLISHED);
            biography.setModerationStatus(Biography.ModerationStatus.APPROVED);
        }

        Biography result = biographyDao.save(biography);

        biographyCategoryBiographyService.addCategoriesToBiography(
                biographyRequest.getAddCategories(),
                result.getId()
        );

        result.setCategories(biographyRequest.getAddCategories());

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

    public Page<Biography> getBiographies(OffsetLimitPageRequest pageRequest,
                                          String categoryName,
                                          Boolean autobiographies) {
        List<FilterCriteria> criteria = new ArrayList<>();

        if (autobiographies != null) {
            criteria.add(
                    new FilterCriteria<>(
                            "creator_id",
                            FilterOperation.FIELDS_EQ,
                            null,
                            "user_id",
                            false
                    )
            );
        }

        return getBiographies(pageRequest, criteria, categoryName);
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

    public int delete(int biographyId) {
        return biographyDao.delete(biographyId);
    }


    public int publish(Integer biographyId) {
        return publishUpdate(biographyId, Biography.PublishStatus.PUBLISHED);
    }

    public int unpublish(Integer biographyId) {
        return publishUpdate(biographyId, Biography.PublishStatus.NOT_PUBLISHED);
    }

    private int publishUpdate(int biographyId, Biography.PublishStatus publishStatus) {
        List<UpdateValue> updateValues = new ArrayList<>();

        updateValues.add(
                new UpdateValue<>(
                        "publish_status",
                        publishStatus.getCode(),
                        true,
                        PreparedStatement::setInt
                )
        );

        List<FilterCriteria> criteria = new ArrayList<>();

        criteria.add(
                new FilterCriteria<>(
                        "id",
                        FilterOperation.EQ,
                        PreparedStatement::setInt,
                        biographyId,
                        true
                )
        );

        return biographyDao.updateValues(updateValues, criteria);
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

        for (Biography biography : biographies) {
            biography.setLikesCount(biographiesLikesCount.get(biography.getId()));
            biography.setLiked(biographiesIsLiked.get(biography.getId()));
            biography.setCommentsCount(biographiesCommentsCount.get(biography.getId()));
            biography.setCategories(biographiesCategories.get(biography.getId()));
        }
    }
}
