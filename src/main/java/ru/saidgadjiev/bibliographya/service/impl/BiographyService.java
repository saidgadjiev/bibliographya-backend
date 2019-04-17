package ru.saidgadjiev.bibliographya.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.saidgadjiev.bibliographya.dao.impl.BiographyDao;
import ru.saidgadjiev.bibliographya.dao.impl.GeneralDao;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.FilterOperation;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.domain.*;
import ru.saidgadjiev.bibliographya.domain.builder.BiographyBuilder;
import ru.saidgadjiev.bibliographya.model.BiographyBaseResponse;
import ru.saidgadjiev.bibliographya.model.BiographyRequest;
import ru.saidgadjiev.bibliographya.model.BiographyUpdateRequest;
import ru.saidgadjiev.bibliographya.model.OffsetLimitPageRequest;

import javax.script.ScriptException;
import java.net.MalformedURLException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Created by said on 22.10.2018.
 */
@Service
public class BiographyService {

    private final MediaService mediaService;

    private final BiographyDao biographyDao;

    private final GeneralDao generalDao;

    private BiographyBuilder biographyBuilder;

    private SecurityService securityService;

    private BiographyCategoryBiographyService biographyCategoryBiographyService;

    @Autowired
    public BiographyService(MediaService mediaService,
                            BiographyDao biographyDao,
                            GeneralDao generalDao,
                            BiographyBuilder biographyBuilder) {
        this.mediaService = mediaService;
        this.biographyDao = biographyDao;
        this.generalDao = generalDao;
        this.biographyBuilder = biographyBuilder;
    }

    @Autowired
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Autowired
    public void setBiographyCategoryBiographyService(BiographyCategoryBiographyService biographyCategoryBiographyService) {
        this.biographyCategoryBiographyService = biographyCategoryBiographyService;
    }

    @Transactional
    public void create(TimeZone timeZone, BiographyRequest biographyRequest) throws SQLException, MalformedURLException {
        User userDetails = (User) securityService.findLoggedInUser();

        Biography biography = new Biography();

        biography.setFirstName(biographyRequest.getFirstName());
        biography.setLastName(biographyRequest.getLastName());
        biography.setMiddleName(biographyRequest.getMiddleName());
        biography.setBio(biographyRequest.getBio());
        biography.setCreatorId(userDetails.getId());

        biographyDao.create(biography);

        if (biographyRequest.getAddCategories() != null && !biographyRequest.getAddCategories().isEmpty()) {
            biographyCategoryBiographyService.addCategoriesToBiography(
                    biographyRequest.getAddCategories(),
                    biography.getId()
            );
        }
        if (StringUtils.isNotBlank(biography.getBio())) {
            String bio = mediaService.storeMedia(biography.getId(), biography.getBio());

            List<UpdateValue> updateValues = new ArrayList<>();

            updateValues.add(
                    new UpdateValue<>(
                            Biography.BIO,
                            bio,
                            PreparedStatement::setString
                    )
            );

            List<FilterCriteria> criteria = new ArrayList<>();

            criteria.add(
                    new FilterCriteria.Builder<Integer>()
                            .propertyName(Biography.ID)
                            .filterValue(biography.getId())
                            .filterOperation(FilterOperation.EQ)
                            .needPreparedSet(true)
                            .valueSetter(PreparedStatement::setInt)
                            .build()
            );

            biographyDao.updateValues(timeZone, updateValues, criteria);
        }
    }

    public Biography createAccountBiography(User user, BiographyRequest biographyRequest) throws SQLException {
        Biography biography = new Biography();

        biography.setFirstName(biographyRequest.getFirstName());
        biography.setLastName(biographyRequest.getLastName());
        biography.setMiddleName(biographyRequest.getMiddleName());
        biography.setCreatorId(user.getId());
        biography.setUserId(user.getId());
        biography.setModerationStatus(Biography.ModerationStatus.APPROVED);

        biographyDao.create(biography);

        return biography;
    }

    public Biography getBiographyById(TimeZone timeZone, int id) {
        User user = (User) securityService.findLoggedInUser();
        Collection<String> fields = new ArrayList<>();

        fields.add(Biography.CREATOR_ID);
        Collection<FilterCriteria> criteria = new ArrayList<>();

        if (user != null) {
            fields.add(Biography.IS_LIKED);

            criteria.addAll(isLikedCriteria());
        }

        Biography biography = biographyDao.getById(timeZone, id, criteria, fields);

        if (biography == null) {
            return null;
        }

        return biographyBuilder.builder(biography).buildCategories().build();
    }

    public Biography getBiographyByCriteria(TimeZone timeZone, Collection<FilterCriteria> criteria) {
        User user = (User) securityService.findLoggedInUser();
        Collection<String> fields = new ArrayList<>();

        fields.add(Biography.CREATOR_ID);
        Collection<FilterCriteria> isLikedCriteria = new ArrayList<>();

        if (user != null) {
            fields.add(Biography.IS_LIKED);

            isLikedCriteria.addAll(isLikedCriteria());
        }

        Biography biography = biographyDao.getByCriteria(timeZone, criteria, isLikedCriteria, fields);

        if (biography == null) {
            return null;
        }

        return biographyBuilder.builder(biography).buildCategories().build();
    }

    public Page<Biography> getBiographies(TimeZone timeZone,
                                          OffsetLimitPageRequest pageRequest,
                                          Integer categoryId,
                                          Boolean autobiographies,
                                          Integer biographyClampSize) throws ScriptException, NoSuchMethodException {
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

        return getBiographies(timeZone, pageRequest, criteria, categoryId, biographyClampSize);
    }


    public Page<Biography> getBiographies(TimeZone timeZone,
                                          OffsetLimitPageRequest pageRequest,
                                          Collection<FilterCriteria> criteria,
                                          Integer categoryId,
                                          Integer biographyClampSize) throws ScriptException, NoSuchMethodException {
        Collection<String> fields = new ArrayList<>();

        fields.add(Biography.CREATOR_ID);
        User userDetails = (User) securityService.findLoggedInUser();
        Collection<FilterCriteria> isLikedCriteria = new ArrayList<>();

        if (userDetails != null) {
            fields.add(Biography.IS_LIKED);

            isLikedCriteria.addAll(isLikedCriteria());
        }

        List<Biography> biographies = biographyDao.getBiographiesList(
                timeZone,
                pageRequest.getPageSize(),
                pageRequest.getOffset(),
                categoryId,
                criteria,
                isLikedCriteria,
                fields,
                pageRequest.getSort()
        );

        if (biographies.isEmpty()) {
            return new PageImpl<>(biographies, pageRequest, 0);
        }

        biographies = biographyBuilder.builder(biographies)
                .buildCategories()
                .truncateBiography(biographyClampSize)
                .build();

        return new PageImpl<>(biographies, pageRequest, biographies.size());
    }


    public Page<Biography> getMyBiographies(TimeZone timeZone, OffsetLimitPageRequest pageRequest, Integer biographyClampSize) throws ScriptException, NoSuchMethodException {
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
                timeZone,
                pageRequest.getPageSize(),
                pageRequest.getOffset(),
                null,
                criteria,
                isLikedCriteria(),
                Arrays.asList(Biography.CREATOR_ID, Biography.IS_LIKED),
                null
        );

        if (biographies.isEmpty()) {
            return new PageImpl<>(biographies, pageRequest, 0);
        }

        biographies = biographyBuilder.builder(biographies)
                .buildCategories()
                .truncateBiography(biographyClampSize)
                .build();

        long total = biographyDao.countOff();

        return new PageImpl<>(biographies, pageRequest, total);
    }

    @Transactional
    public BiographyUpdateStatus update(TimeZone timeZone,
                                        Integer id,
                                        BiographyRequest updateBiographyRequest) throws SQLException, MalformedURLException {
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

        updateBiographyRequest.setBio(mediaService.storeMedia(id, updateBiographyRequest.getBio()));

        updateValues.add(
                new UpdateValue<>(
                        Biography.BIO,
                        updateBiographyRequest.getBio(),
                        PreparedStatement::setString
                )
        );

        if (StringUtils.isBlank(updateBiographyRequest.getBio())) {
            updateValues.add(
                    new UpdateValue<>(
                            Biography.PUBLISH_STATUS,
                            Biography.PublishStatus.NOT_PUBLISHED.getCode(),
                            PreparedStatement::setInt
                    )
            );
        }
        List<FilterCriteria> criteria = new ArrayList<>();

        LocalDateTime updatedAt = updateBiographyRequest.getUpdatedAt().toLocalDateTime();

        ZonedDateTime zonedUpdatedAtDateTime = updatedAt.atZone(timeZone.toZoneId());

        LocalDateTime utcUpdatedAt = zonedUpdatedAtDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();

        criteria.add(
                new FilterCriteria.Builder<Timestamp>()
                        .propertyName(Biography.UPDATED_AT)
                        .filterOperation(FilterOperation.EQ)
                        .filterValue(Timestamp.valueOf(utcUpdatedAt))
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

        BiographyUpdateStatus status = biographyDao.updateValues(timeZone, updateValues, criteria);

        if (status.getUpdated() > 0) {
            status.setBio(updateBiographyRequest.getBio());

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


    public int publish(TimeZone timeZone, Integer biographyId) {
        return publishUpdate(timeZone, biographyId, Biography.PublishStatus.PUBLISHED);
    }

    public int unPublish(TimeZone timeZone, Integer biographyId) {
        return publishUpdate(timeZone, biographyId, Biography.PublishStatus.NOT_PUBLISHED);
    }

    public boolean isIAuthor(int biographyId) {
        List<Map<String, Object>> result = generalDao.getFields(
                Biography.TABLE,
                Collections.singletonList(Biography.CREATOR_ID),
                Collections.singletonList(
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

    public BiographiesStats getStats() {
        BiographiesStats biographiesStats = new BiographiesStats();

        biographiesStats.setCount(biographyDao.countOff());

        return biographiesStats;
    }


    public RequestResult<Biography> partialUpdate(TimeZone timeZone, int biographyId, BiographyUpdateRequest updateRequest) {
        List<UpdateValue> updateValues = new ArrayList<>();
        Collection<FilterCriteria> criteria = new ArrayList<>();

        criteria.add(
                new FilterCriteria.Builder<Integer>()
                        .propertyName(Biography.ID)
                        .filterOperation(FilterOperation.EQ)
                        .filterValue(biographyId)
                        .valueSetter(PreparedStatement::setInt)
                        .needPreparedSet(true)
                        .build()
        );

        if (updateRequest.getAnonymousCreator() != null) {
            updateValues.add(
                    new UpdateValue<>(Biography.ANONYMOUS_CREATOR, updateRequest.getAnonymousCreator(), PreparedStatement::setBoolean)
            );
        }

        if (updateRequest.getDisableComments() != null) {
            updateValues.add(
                    new UpdateValue<>(Biography.DISABLE_COMMENTS, updateRequest.getDisableComments(), PreparedStatement::setBoolean)
            );
        }

        int update = generalDao.update(
                Biography.TABLE,
                updateValues,
                criteria,
                null
        );

        Biography biography = null;

        if (updateRequest.getReturnFields() != null && !updateRequest.getReturnFields().isEmpty()) {
            Collection<Biography> biographies = biographyDao.getFields(timeZone, normalizeFields(updateRequest.getReturnFields()), criteria);

            if (!biographies.isEmpty()) {
                biography = biographies.iterator().next();
            }
        }

        return new RequestResult<Biography>().setStatus(update == 1 ? HttpStatus.OK : HttpStatus.NOT_FOUND).setBody(biography);
    }

    private int publishUpdate(TimeZone timeZone, int biographyId, Biography.PublishStatus publishStatus) {
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

        if (publishStatus == Biography.PublishStatus.PUBLISHED) {
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
                                .propertyName(Biography.BIO)
                                .filterOperation(FilterOperation.IS_NOT_NULL)
                                .needPreparedSet(false)
                                .build()
                );
                criteria.add(
                        new FilterCriteria.Builder<String>()
                                .propertyName(Biography.BIO)
                                .filterOperation(FilterOperation.NOT_EQ)
                                .filterValue("")
                                .needPreparedSet(true)
                                .valueSetter(PreparedStatement::setString)
                                .build()
                );
            }
        }

        return biographyDao.updateValues(timeZone, updateValues, criteria).getUpdated();
    }

    private Collection<FilterCriteria> isLikedCriteria() {
        User user = (User) securityService.findLoggedInUser();

        Collection<FilterCriteria> isLikedCriteria = new ArrayList<>();

        isLikedCriteria.add(
                new FilterCriteria.Builder<Integer>()
                        .propertyName(BiographyLike.USER_ID)
                        .filterOperation(FilterOperation.EQ)
                        .filterValue(user.getId())
                        .valueSetter(PreparedStatement::setInt)
                        .needPreparedSet(true)
                        .build()
        );

        return isLikedCriteria;
    }

    private Collection<String> normalizeFields(Collection<String> fields) {
        Collection<String> normalizedFields = new ArrayList<>();

        for (String field: fields) {
            switch (field) {
                case BiographyBaseResponse.ANONYMOUS_CREATOR:
                    normalizedFields.add(Biography.ANONYMOUS_CREATOR);
                    break;
                case BiographyBaseResponse.DISABLE_COMMENTS:
                    normalizedFields.add(Biography.DISABLE_COMMENTS);
                    break;
                case BiographyBaseResponse.CREATOR_ID:
                    normalizedFields.add(Biography.CREATOR_ID);
                    break;
            }
        }

        return normalizedFields;
    }
}
