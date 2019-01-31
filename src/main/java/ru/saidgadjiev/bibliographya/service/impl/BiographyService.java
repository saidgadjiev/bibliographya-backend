package ru.saidgadjiev.bibliographya.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.saidgadjiev.bibliographya.dao.api.BiographyDao;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.FilterOperation;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.BiographyUpdateStatus;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.model.BiographyRequest;
import ru.saidgadjiev.bibliographya.model.OffsetLimitPageRequest;

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

    private SecurityService securityService;

    private BiographyCommentService biographyCommentService;

    private BiographyLikeService biographyLikeService;

    private BiographyCategoryBiographyService biographyCategoryBiographyService;

    @Autowired
    public BiographyService(@Qualifier("sql") BiographyDao biographyDao) {
        this.biographyDao = biographyDao;
    }

    @Autowired
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Autowired
    public void setBiographyCommentService(BiographyCommentService biographyCommentService) {
        this.biographyCommentService = biographyCommentService;
    }

    @Autowired
    public void setBiographyLikeService(BiographyLikeService biographyLikeService) {
        this.biographyLikeService = biographyLikeService;
    }

    @Autowired
    public void setBiographyCategoryBiographyService(BiographyCategoryBiographyService biographyCategoryBiographyService) {
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

        Biography result = biographyDao.save(biography);

        if (biographyRequest.getAddCategories() != null && !biographyRequest.getAddCategories().isEmpty()) {
            biographyCategoryBiographyService.addCategoriesToBiography(
                    biographyRequest.getAddCategories(),
                    result.getId()
            );
        }

        result.setCategories(biographyRequest.getAddCategories());

        return result;
    }

    public Biography createAccountBiography(User user, BiographyRequest biographyRequest) throws SQLException {
        Collection<UpdateValue> updateValues = new ArrayList<>();

        updateValues.add(
                new UpdateValue<>(
                        Biography.FIRST_NAME,
                        biographyRequest.getFirstName(),
                        PreparedStatement::setString
                )
        );
        updateValues.add(
                new UpdateValue<>(
                        Biography.LAST_NAME,
                        biographyRequest.getLastName(),
                        PreparedStatement::setString
                )
        );

        if (StringUtils.isNotBlank(biographyRequest.getMiddleName())) {
            updateValues.add(
                    new UpdateValue<>(
                            Biography.MIDDLE_NAME,
                            biographyRequest.getMiddleName(),
                            PreparedStatement::setString
                    )
            );
        }

        updateValues.add(
                new UpdateValue<>(
                        Biography.CREATOR_ID,
                        user.getId(),
                        PreparedStatement::setInt
                )
        );

        updateValues.add(
                new UpdateValue<>(
                        Biography.USER_ID,
                        user.getId(),
                        PreparedStatement::setInt
                )
        );

        updateValues.add(
                new UpdateValue<>(
                        Biography.MODERATION_STATUS,
                        Biography.ModerationStatus.APPROVED.getCode(),
                        PreparedStatement::setInt
                )
        );

        return biographyDao.save(updateValues);
    }

    public Biography getShortBiography(int userId) {
        List<FilterCriteria> criteria = new ArrayList<>();

        criteria.add(
                new FilterCriteria.Builder<Integer>()
                        .propertyName(Biography.USER_ID)
                        .filterValue(userId)
                        .filterOperation(FilterOperation.EQ)
                        .needPreparedSet(true)
                        .valueSetter(PreparedStatement::setInt)
                        .build()
        );
        List<Map<String, Object>> fields = biographyDao.getFields(Arrays.asList(Biography.ID, Biography.FIRST_NAME, Biography.LAST_NAME), criteria);

        if (fields == null || fields.isEmpty()) {
            return null;
        }
        Map<String, Object> result = fields.iterator().next();

        Biography biography = new Biography();

        biography.setId((Integer) result.get(Biography.ID));
        biography.setFirstName((String) result.get(Biography.FIRST_NAME));
        biography.setLastName((String) result.get(Biography.LAST_NAME));

        return biography;
    }

    public Biography getCurrentUserShortBiography() {
        User user = (User) securityService.findLoggedInUser();

        return getShortBiography(user.getId());
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
                            Biography.USER_ID,
                            FilterOperation.IS_NOT_NULL,
                            null,
                            null,
                            false
                    )
            );
        }
        criteria.add(
                new FilterCriteria.Builder<Integer>()
                        .propertyName(Biography.PUBLISH_STATUS)
                        .filterOperation(FilterOperation.EQ)
                        .valueSetter(PreparedStatement::setInt)
                        .filterValue(Biography.PublishStatus.PUBLISHED.getCode())
                        .needPreparedSet(true)
                        .build()
        );

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
                new FilterCriteria<>(
                        Biography.CREATOR_ID,
                        FilterOperation.EQ,
                        PreparedStatement::setInt,
                        user.getId(),
                        true
                )
        );
        criteria.add(
                new FilterCriteria.Builder<Boolean>()
                        .propertyName(Biography.USER_ID)
                        .filterOperation(FilterOperation.IS_NULL)
                        .needPreparedSet(false)
                        .build()
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
        List<UpdateValue> updateValues = new ArrayList<>();

        updateValues.add(
                new UpdateValue<>(
                        Biography.FIRST_NAME,
                        updateBiographyRequest.getFirstName(),
                        PreparedStatement::setString
                )
        );
        updateValues.add(
                new UpdateValue<>(
                        Biography.LAST_NAME,
                        updateBiographyRequest.getLastName(),
                        PreparedStatement::setString
                )
        );
        updateValues.add(
                new UpdateValue<>(
                        Biography.MIDDLE_NAME,
                        updateBiographyRequest.getMiddleName(),
                        PreparedStatement::setString
                )
        );
        updateValues.add(
                new UpdateValue<>(
                        Biography.BIOGRAPHY,
                        updateBiographyRequest.getBiography(),
                        PreparedStatement::setString
                )
        );

        if (StringUtils.isBlank(updateBiographyRequest.getBiography())) {
            updateValues.add(
                    new UpdateValue<>(
                            Biography.PUBLISH_STATUS,
                            Biography.PublishStatus.NOT_PUBLISHED.getCode(),
                            PreparedStatement::setInt
                    )
            );
        }
        List<FilterCriteria> criteria = new ArrayList<>();

        Timestamp timestamp = new Timestamp(updateBiographyRequest.getLastModified().getTime());

        timestamp.setNanos(updateBiographyRequest.getLastModified().getNanos());

        criteria.add(
                new FilterCriteria.Builder<Timestamp>()
                        .propertyName(Biography.UPDATED_AT)
                        .filterOperation(FilterOperation.EQ)
                        .filterValue(timestamp)
                        .needPreparedSet(true)
                        .valueSetter(PreparedStatement::setTimestamp)
                        .build()
        );
        criteria.add(
                new FilterCriteria.Builder<Integer>()
                        .propertyName(Biography.ID)
                        .filterValue(id)
                        .filterOperation(FilterOperation.EQ)
                        .needPreparedSet(true)
                        .valueSetter(PreparedStatement::setInt)
                        .build()
        );

        BiographyUpdateStatus status = biographyDao.updateValues(updateValues, criteria);

        if (status.getUpdated() > 0) {
            if (updateBiographyRequest.getAddCategories() != null && !updateBiographyRequest.getAddCategories().isEmpty()) {
                biographyCategoryBiographyService.addCategoriesToBiography(
                        updateBiographyRequest.getAddCategories(),
                        id
                );
            }
            if (updateBiographyRequest.getDeleteCategories() != null && !updateBiographyRequest.getDeleteCategories().isEmpty()) {
                biographyCategoryBiographyService.deleteCategoriesFromBiography(
                        updateBiographyRequest.getDeleteCategories(),
                        id
                );
            }
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

    public boolean isIAuthor(int biographyId) {
        List<Map<String, Object>> result = biographyDao.getFields(Collections.singletonList(Biography.CREATOR_ID), Collections.singletonList(
                new FilterCriteria.Builder<Integer>()
                        .propertyName(Biography.ID)
                        .filterOperation(FilterOperation.EQ)
                        .valueSetter(PreparedStatement::setInt)
                        .filterValue(biographyId)
                        .build()
        ));

        if (result.isEmpty()) {
            return false;
        }
        Integer creatorId = (Integer) result.iterator().next().get(Biography.CREATOR_ID);
        User user = (User) securityService.findLoggedInUser();

        return Objects.equals(creatorId, user.getId());
    }

    private int publishUpdate(int biographyId, Biography.PublishStatus publishStatus) {
        List<UpdateValue> updateValues = new ArrayList<>();

        updateValues.add(
                new UpdateValue<>(
                        Biography.PUBLISH_STATUS,
                        publishStatus.getCode(),
                        PreparedStatement::setInt
                )
        );

        List<FilterCriteria> criteria = new ArrayList<>();

        criteria.add(
                new FilterCriteria<>(
                        Biography.ID,
                        FilterOperation.EQ,
                        PreparedStatement::setInt,
                        biographyId,
                        true
                )
        );
        criteria.add(
                new FilterCriteria.Builder<Integer>()
                        .propertyName(Biography.MODERATION_STATUS)
                        .filterOperation(FilterOperation.EQ)
                        .filterValue(Biography.ModerationStatus.APPROVED.getCode())
                        .valueSetter(PreparedStatement::setInt)
                        .build()
        );

        if (publishStatus.equals(Biography.PublishStatus.PUBLISHED)) {
            criteria.add(
                    new FilterCriteria.Builder<String>()
                            .propertyName(Biography.BIOGRAPHY)
                            .filterOperation(FilterOperation.IS_NOT_NULL)
                            .needPreparedSet(false)
                            .build()
            );
            criteria.add(
                    new FilterCriteria.Builder<String>()
                            .propertyName(Biography.BIOGRAPHY)
                            .filterOperation(FilterOperation.NOT_EQ)
                            .filterValue("")
                            .needPreparedSet(true)
                            .valueSetter(PreparedStatement::setString)
                            .build()
            );
        }

        return biographyDao.updateValues(updateValues, criteria).getUpdated();
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
