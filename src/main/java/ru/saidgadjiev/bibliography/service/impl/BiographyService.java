package ru.saidgadjiev.bibliography.service.impl;

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
        Biography biography = new Biography();

        biography.setFirstName(biographyRequest.getFirstName());
        biography.setLastName(biographyRequest.getLastName());
        biography.setMiddleName(biographyRequest.getMiddleName());
        biography.setCreatorId(user.getId());
        biography.setUserId(user.getId());
        biography.setIsAutobiography(true);
        biography.setModerationStatus(Biography.ModerationStatus.APPROVED);

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
                            "is_autobiography",
                            FilterOperation.EQ,
                            PreparedStatement::setBoolean,
                            true,
                            true
                    )
            );
        }
        criteria.add(
                new FilterCriteria.Builder<Integer>()
                        .propertyName("publish_status")
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
                        "creator_id",
                        FilterOperation.EQ,
                        PreparedStatement::setInt,
                        user.getId(),
                        true
                )
        );
        criteria.add(
                new FilterCriteria.Builder<Boolean>()
                        .propertyName("is_autobiography")
                        .filterOperation(FilterOperation.EQ)
                        .valueSetter(PreparedStatement::setBoolean)
                        .filterValue(false)
                        .needPreparedSet(true)
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

    //TODO: переделать обновление с UpdateValue
    @Transactional
    public BiographyUpdateStatus update(Integer id, BiographyRequest updateBiographyRequest) throws SQLException {
        List<UpdateValue> updateValues = new ArrayList<>();

        updateValues.add(
                new UpdateValue<>(
                        "first_name",
                        updateBiographyRequest.getFirstName(),
                        true,
                        PreparedStatement::setString
                )
        );
        updateValues.add(
                new UpdateValue<>(
                        "last_name",
                        updateBiographyRequest.getLastName(),
                        true,
                        PreparedStatement::setString
                )
        );
        updateValues.add(
                new UpdateValue<>(
                        "middle_name",
                        updateBiographyRequest.getMiddleName(),
                        true,
                        PreparedStatement::setString
                )
        );
        updateValues.add(
                new UpdateValue<>(
                        "biography",
                        updateBiographyRequest.getBiography(),
                        true,
                        PreparedStatement::setString
                )
        );

        if (isMyBiography(updateBiographyRequest.getUserId()) &&
                StringUtils.isBlank(updateBiographyRequest.getBiography())) {
            updateValues.add(
                    new UpdateValue<>(
                            "publish_status",
                            Biography.PublishStatus.NOT_PUBLISHED.getCode(),
                            true,
                            PreparedStatement::setInt
                    )
            );
        }
        List<FilterCriteria> criteria = new ArrayList<>();

        Timestamp timestamp = new Timestamp(updateBiographyRequest.getLastModified().getTime());

        timestamp.setNanos(updateBiographyRequest.getLastModified().getNanos());

        criteria.add(
                new FilterCriteria.Builder<Timestamp>()
                        .propertyName("updated_at")
                        .filterOperation(FilterOperation.EQ)
                        .filterValue(timestamp)
                        .needPreparedSet(true)
                        .valueSetter(PreparedStatement::setTimestamp)
                        .build()
        );
        criteria.add(
                new FilterCriteria.Builder<Integer>()
                        .propertyName("id")
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

        if (publishStatus.equals(Biography.PublishStatus.PUBLISHED)) {
            criteria.add(
                    new FilterCriteria.Builder<String>()
                            .propertyName("biography")
                            .filterOperation(FilterOperation.IS_NOT_NULL)
                            .needPreparedSet(false)
                            .build()
            );
            criteria.add(
                    new FilterCriteria.Builder<String>()
                            .propertyName("biography")
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

    private boolean isMyBiography(Integer userId) {
        User user = (User) securityService.findLoggedInUser();

        return Objects.equals(user.getId(), userId);
    }
}
