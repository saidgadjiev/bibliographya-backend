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
import ru.saidgadjiev.bibliographya.data.PreparedSetter;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.column.ColumnSpec;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.*;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.function.Lower;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.literals.IntLiteral;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.literals.Param;
import ru.saidgadjiev.bibliographya.domain.*;
import ru.saidgadjiev.bibliographya.domain.builder.BiographyBuilder;
import ru.saidgadjiev.bibliographya.model.BiographyBaseResponse;
import ru.saidgadjiev.bibliographya.model.BiographyRequest;
import ru.saidgadjiev.bibliographya.model.BiographyUpdateRequest;
import ru.saidgadjiev.bibliographya.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliographya.utils.RoleUtils;

import javax.script.ScriptException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
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

    private ProfessionService professionService;

    @Autowired
    public BiographyService(MediaService mediaService,
                            BiographyDao biographyDao,
                            GeneralDao generalDao,
                            BiographyBuilder biographyBuilder,
                            ProfessionService professionService) {
        this.mediaService = mediaService;
        this.biographyDao = biographyDao;
        this.generalDao = generalDao;
        this.biographyBuilder = biographyBuilder;
        this.professionService = professionService;
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
        biography.setCountryId(biographyRequest.getCountryId());

        biographyDao.create(biography);

        if (biographyRequest.getAddCategories() != null && !biographyRequest.getAddCategories().isEmpty()) {
            biographyCategoryBiographyService.addCategoriesToBiography(
                    biographyRequest.getAddCategories(),
                    biography.getId()
            );
        }
        if (biographyRequest.getAddProfessions() != null && !biographyRequest.getAddProfessions().isEmpty()) {
            professionService.addProfessionsToBiography(
                    biographyRequest.getAddProfessions(),
                    biography.getId()
            );
        }
        if (RoleUtils.hasAnyRole(userDetails.getRoles(), Role.ROLE_MODERATOR)) {
            String bio = mediaService.storeMedia(biography.getId(), biography.getBio());

            List<UpdateValue> updateValues = new ArrayList<>();

            updateValues.add(
                    new UpdateValue<>(
                            Biography.BIO,
                            (preparedStatement, index) -> preparedStatement.setString(index, bio)
                    )
            );

            AndCondition andCondition = new AndCondition();

            andCondition.add(new Equals(new ColumnSpec(Biography.ID), new Param()));
            List<PreparedSetter> values = new ArrayList<>();

            values.add((preparedStatement, index) -> preparedStatement.setInt(index, biography.getId()));

            biographyDao.updateValues(timeZone, updateValues, andCondition, values);
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
        AndCondition andCondition = new AndCondition();
        List<PreparedSetter> values = new ArrayList<>();

        if (user != null) {
            fields.add(Biography.IS_LIKED);

            andCondition.add(isLikedCriteria());
        }

        Biography biography = biographyDao.getById(timeZone, id, andCondition, values, fields);

        if (biography == null) {
            return null;
        }

        return biographyBuilder.builder(biography).buildCategoriesAndProfessions().build();
    }

    public Biography getBiographyByCriteria(TimeZone timeZone, AndCondition andCondition, List<PreparedSetter> values) {
        User user = (User) securityService.findLoggedInUser();
        Collection<String> fields = new ArrayList<>();

        fields.add(Biography.CREATOR_ID);
        AndCondition isLikedCondition = new AndCondition();

        if (user != null) {
            fields.add(Biography.IS_LIKED);

            isLikedCondition.add(isLikedCriteria());
        }

        Biography biography = biographyDao.getByCriteria(timeZone, andCondition, isLikedCondition, values, fields);

        if (biography == null) {
            return null;
        }

        return biographyBuilder.builder(biography).buildCategoriesAndProfessions().build();
    }

    public Page<Biography> getBiographies(TimeZone timeZone,
                                          OffsetLimitPageRequest pageRequest,
                                          Integer categoryId,
                                          Boolean autobiographies,
                                          Integer biographyClampSize,
                                          String query) throws ScriptException, NoSuchMethodException {
        AndCondition andCondition = new AndCondition();
        List<PreparedSetter> values = new ArrayList<>();

        if (StringUtils.isNotBlank(query)) {
            List<String> terms = Arrays.asList(query.split(" "));
            StringBuilder pattern = new StringBuilder();

            pattern.append("(");

            for (Iterator<String> iterator = terms.iterator(); iterator.hasNext(); ) {
                pattern.append(iterator.next().toLowerCase());

                if (iterator.hasNext()) {
                    pattern.append("|");
                }
            }

            pattern.append(")%");

            andCondition = new AndCondition() {{
                add(
                        new Expression() {{
                            add(new AndCondition() {{
                                add(new Similar(new Lower(new ColumnSpec(Biography.FIRST_NAME)), pattern.toString()));
                            }});
                            add(new AndCondition() {{
                                add(new Similar(new Lower(new ColumnSpec(Biography.LAST_NAME)), pattern.toString()));
                            }});
                            add(new AndCondition() {{
                                add(new Similar(new Lower(new ColumnSpec(Biography.MIDDLE_NAME)), pattern.toString()));
                            }});
                        }}
                );
            }};
        }

        andCondition.add(new Equals(new ColumnSpec(Biography.PUBLISH_STATUS), new Param()));

        if (autobiographies != null) {
            andCondition.add(new NotNull(new ColumnSpec(Biography.USER_ID)));
        }

        values.add((preparedStatement, index) -> preparedStatement.setInt(index, Biography.PublishStatus.PUBLISHED.getCode()));

        return getBiographies(timeZone, pageRequest, andCondition, values, categoryId, biographyClampSize);
    }


    public Page<Biography> getBiographies(TimeZone timeZone,
                                          OffsetLimitPageRequest pageRequest,
                                          AndCondition condition,
                                          List<PreparedSetter> values,
                                          Integer categoryId,
                                          Integer biographyClampSize
    ) throws ScriptException, NoSuchMethodException {
        Collection<String> fields = new ArrayList<>();

        fields.add(Biography.CREATOR_ID);
        User userDetails = (User) securityService.findLoggedInUser();
        AndCondition likeCondition = new AndCondition();

        if (userDetails != null) {
            fields.add(Biography.IS_LIKED);

            likeCondition.add(isLikedCriteria());
        }

        List<Biography> biographies = biographyDao.getBiographiesList(
                timeZone,
                pageRequest.getPageSize(),
                pageRequest.getOffset(),
                categoryId,
                condition,
                likeCondition,
                values,
                fields,
                pageRequest.getSort()
        );

        if (biographies.isEmpty()) {
            return new PageImpl<>(biographies, pageRequest, 0);
        }

        biographies = biographyBuilder.builder(biographies)
                .buildCategoriesAndProfessions()
                .truncateBiography(biographyClampSize)
                .build();

        return new PageImpl<>(biographies, pageRequest, biographies.size());
    }


    public Page<Biography> getMyBiographies(TimeZone timeZone, OffsetLimitPageRequest pageRequest, Integer biographyClampSize) throws ScriptException, NoSuchMethodException {
        User user = (User) securityService.findLoggedInUser();
        List<PreparedSetter> values = new ArrayList<>();

        AndCondition criteria = new AndCondition() {{
            add(new Equals(new ColumnSpec(Biography.CREATOR_ID), new Param()));

            values.add((preparedStatement, index) -> preparedStatement.setInt(index, user.getId()));

            add(new IsNull(new ColumnSpec(Biography.USER_ID)));
        }};

        List<Biography> biographies = biographyDao.getBiographiesList(
                timeZone,
                pageRequest.getPageSize(),
                pageRequest.getOffset(),
                null,
                criteria,
                new AndCondition() {{
                    add(isLikedCriteria());
                }},
                values,
                Arrays.asList(Biography.CREATOR_ID, Biography.IS_LIKED),
                pageRequest.getSort()
        );

        if (biographies.isEmpty()) {
            return new PageImpl<>(biographies, pageRequest, 0);
        }

        biographies = biographyBuilder.builder(biographies)
                .buildCategoriesAndProfessions()
                .truncateBiography(biographyClampSize)
                .build();

        long total = biographyDao.countOff();

        return new PageImpl<>(biographies, pageRequest, total);
    }

    @Transactional
    public BiographyUpdateStatus update(TimeZone timeZone,
                                        Integer id,
                                        BiographyRequest updateBiographyRequest) throws MalformedURLException {
        List<UpdateValue> updateValues = new ArrayList<>();

        updateValues.add(
                new UpdateValue<>(
                        Biography.FIRST_NAME,
                        (preparedStatement, index) -> preparedStatement.setString(index, updateBiographyRequest.getFirstName())
                )
        );
        updateValues.add(
                new UpdateValue<>(
                        Biography.LAST_NAME,
                        (preparedStatement, index) -> preparedStatement.setString(index, updateBiographyRequest.getLastName())
                )
        );
        updateValues.add(
                new UpdateValue<>(
                        Biography.MIDDLE_NAME,
                        (preparedStatement, index) -> {
                            if (StringUtils.isBlank(updateBiographyRequest.getMiddleName())) {
                                preparedStatement.setNull(index, Types.VARCHAR);
                            } else {
                                preparedStatement.setString(index, updateBiographyRequest.getMiddleName());
                            }
                        }
                )
        );
        updateValues.add(
                new UpdateValue<>(
                        Biography.COUNTRY_ID,
                        (preparedStatement, index) -> {
                            if (updateBiographyRequest.getCountryId() == null) {
                                preparedStatement.setNull(index, Types.INTEGER);
                            } else {
                                preparedStatement.setInt(index, updateBiographyRequest.getCountryId());
                            }
                        }
                )
        );

        User user = (User) securityService.findLoggedInUser();

        if (RoleUtils.hasAnyRole(user.getRoles(), Role.ROLE_MODERATOR)) {
            updateBiographyRequest.setBio(mediaService.storeMedia(id, updateBiographyRequest.getBio()));
        }

        updateValues.add(
                new UpdateValue<>(
                        Biography.BIO,
                        (preparedStatement, index) -> {
                            if (StringUtils.isBlank(updateBiographyRequest.getBio())) {
                                preparedStatement.setNull(index, Types.VARCHAR);
                            } else {
                                preparedStatement.setString(index, updateBiographyRequest.getBio());
                            }
                        }
                )
        );

        if (StringUtils.isBlank(updateBiographyRequest.getBio())) {
            updateValues.add(
                    new UpdateValue<>(
                            Biography.PUBLISH_STATUS,
                            (preparedStatement, index) -> preparedStatement.setInt(index, Biography.PublishStatus.NOT_PUBLISHED.getCode())
                    )
            );
        }

        LocalDateTime updatedAt = updateBiographyRequest.getUpdatedAt().toLocalDateTime();

        ZonedDateTime zonedUpdatedAtDateTime = updatedAt.atZone(timeZone.toZoneId());

        LocalDateTime utcUpdatedAt = zonedUpdatedAtDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        List<PreparedSetter> values = new ArrayList<>();

        AndCondition criteria = new AndCondition() {{
            add(new Equals(new ColumnSpec(Biography.UPDATED_AT), new Param()));
            values.add((preparedStatement, index) -> preparedStatement.setTimestamp(index, Timestamp.valueOf(utcUpdatedAt)));

            add(new Equals(new ColumnSpec(Biography.ID), new Param()));
            values.add((preparedStatement, index) -> preparedStatement.setInt(index, id));
        }};

        BiographyUpdateStatus status = biographyDao.updateValues(timeZone, updateValues, criteria, values);

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
            if (updateBiographyRequest.getAddProfessions() != null && !updateBiographyRequest.getAddProfessions().isEmpty()) {
                professionService.addProfessionsToBiography(
                        updateBiographyRequest.getAddProfessions(),
                        id
                );
            }
            if (updateBiographyRequest.getDeleteProfessions() != null && !updateBiographyRequest.getDeleteProfessions().isEmpty()) {
                professionService.deleteProfessionsFromBiography(
                        updateBiographyRequest.getDeleteProfessions(),
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
                new AndCondition() {{
                    add(new Equals(new ColumnSpec(Biography.ID), new Param()));
                }},
                Collections.singletonList((preparedStatement, index) -> preparedStatement.setInt(index, biographyId))
        );

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
        AndCondition condition = new AndCondition();

        condition.add(new Equals(new ColumnSpec(Biography.ID), new Param()));

        if (updateRequest.getAnonymousCreator() != null) {
            updateValues.add(
                    new UpdateValue<>(Biography.ANONYMOUS_CREATOR, (preparedStatement, index) -> preparedStatement.setBoolean(index, updateRequest.getAnonymousCreator()))
            );
        }

        if (updateRequest.getDisableComments() != null) {
            updateValues.add(
                    new UpdateValue<>(Biography.ANONYMOUS_CREATOR, (preparedStatement, index) -> preparedStatement.setBoolean(index, updateRequest.getDisableComments()))
            );
        }

        int update = generalDao.update(
                Biography.TABLE,
                updateValues,
                condition,
                Collections.singletonList((preparedStatement, index) -> preparedStatement.setInt(index, biographyId)),
                null
        );

        Biography biography = null;

        if (updateRequest.getReturnFields() != null && !updateRequest.getReturnFields().isEmpty()) {
            Collection<Biography> biographies = biographyDao.getFields(
                    timeZone,
                    normalizeFields(updateRequest.getReturnFields()),
                    condition,
                    Collections.singletonList((preparedStatement, index) -> preparedStatement.setInt(index, biographyId))
            );

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
                        (preparedStatement, index) -> preparedStatement.setInt(index, publishStatus.getCode())
                )
        );

        List<PreparedSetter> values = new ArrayList<>();
        AndCondition criteria = new AndCondition() {{
            add(new Equals(new ColumnSpec(Biography.ID), new Param()));
            values.add((preparedStatement, index) -> preparedStatement.setInt(index, biographyId));
        }};

        if (publishStatus == Biography.PublishStatus.PUBLISHED) {
            criteria.add(
                    new Equals(new ColumnSpec(Biography.MODERATION_STATUS), new Param())
            );
            values.add((preparedStatement, index) -> preparedStatement.setInt(index, Biography.ModerationStatus.APPROVED.getCode()));
            criteria.add(new NotNull(new ColumnSpec(Biography.BIO)));
            criteria.add(new NotEquals(new ColumnSpec(Biography.BIO), new Param()));

            values.add((preparedStatement, index) -> preparedStatement.setString(index, ""));
        }

        return biographyDao.updateValues(timeZone, updateValues, criteria, values).getUpdated();
    }

    private Condition isLikedCriteria() {
        User user = (User) securityService.findLoggedInUser();

        return new Equals(new ColumnSpec(Biography.USER_ID), new IntLiteral(user.getId()));
    }

    private Collection<String> normalizeFields(Collection<String> fields) {
        Collection<String> normalizedFields = new ArrayList<>();

        for (String field : fields) {
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
