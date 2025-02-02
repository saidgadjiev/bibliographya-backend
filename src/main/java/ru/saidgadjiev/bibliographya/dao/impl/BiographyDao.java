package ru.saidgadjiev.bibliographya.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliographya.dao.impl.dsl.DslVisitor;
import ru.saidgadjiev.bibliographya.data.PreparedSetter;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.AndCondition;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.Expression;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.BiographyUpdateStatus;
import ru.saidgadjiev.bibliographya.domain.Country;
import ru.saidgadjiev.bibliographya.utils.ResultSetUtils;
import ru.saidgadjiev.bibliographya.utils.SortUtils;

import java.sql.*;
import java.util.*;

/**
 * Created by said on 22.10.2018.
 */
@Repository
public class BiographyDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public BiographyDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(Biography biography) throws SQLException {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        StringBuilder insertPart = new StringBuilder();
        StringBuilder valuesPart = new StringBuilder();


        valuesPart.append("VALUES(?, ?, ?, ?, ?, ?");

        insertPart.append("INSERT INTO biography(first_name, last_name, middle_name, bio, creator_id, user_id");
        List<PreparedSetter> values = new ArrayList<>();

        if (biography.getModerationStatus() != null) {
            insertPart.append(",moderation_status");
            valuesPart.append(",?");
            values.add((preparedStatement, index) -> {
                preparedStatement.setInt(index, biography.getModerationStatus().getCode());
            });
        }
        if (biography.getCountryId() != null) {
            insertPart.append(",country_id");
            valuesPart.append(",?");
            values.add((preparedStatement, index) -> preparedStatement.setInt(index, biography.getCountryId()));
        }
        insertPart.append(")");
        valuesPart.append(")");

        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(
                            insertPart.toString() + " " + valuesPart.toString(),
                            Statement.RETURN_GENERATED_KEYS
                    );

                    ps.setString(1, biography.getFirstName());
                    ps.setString(2, biography.getLastName());

                    if (StringUtils.isBlank(biography.getMiddleName())) {
                        ps.setNull(3, Types.VARCHAR);
                    } else {
                        ps.setString(3, biography.getMiddleName());
                    }
                    if (StringUtils.isBlank(biography.getBio())) {
                        ps.setNull(4, Types.VARCHAR);
                    } else {
                        ps.setString(4, biography.getBio());
                    }

                    ps.setInt(5, biography.getCreatorId());

                    if (biography.getUserId() == null) {
                        ps.setNull(6, Types.INTEGER);
                    } else {
                        ps.setInt(6, biography.getUserId());
                    }
                    int i = 7;

                    for (PreparedSetter value: values) {
                        value.set(ps, i++);
                    }

                    return ps;
                },
                keyHolder
        );

        Map<String, Object> keys = keyHolder.getKeys();

        if (keys != null && keys.containsKey("id")) {
            biography.setId(((Number) keys.get("id")).intValue());
        }
    }

    public List<Biography> getBiographiesList(TimeZone timeZone,
                                              int limit,
                                              long offset,
                                              Integer categoryId,
                                              AndCondition biographyCriteria,
                                              AndCondition isLikedCriteria,
                                              List<PreparedSetter> values,
                                              Collection<String> fields,
                                              Sort sort
    ) {
        StringBuilder clause = new StringBuilder();

        if (categoryId != null) {
            clause
                    .append("b.id IN (SELECT biography_id FROM biography_category_biography WHERE category_id = '")
                    .append(categoryId)
                    .append("')");
        }

        DslVisitor visitor = new DslVisitor("b");

        new Expression() {{
            add(biographyCriteria);
        }}.accept(visitor);

        String biographyClause = visitor.getClause();

        if (biographyClause.length() > 0) {
            if (clause.length() > 0) {
                clause.append(" AND ");
            }
            clause.append(biographyClause);
        }
        StringBuilder sql = new StringBuilder();

        sql
                .append("SELECT ")
                .append(getFullSelectList(timeZone, fields))
                .append(" FROM biography b");

        appendJoins(sql, fields, isLikedCriteria);

        if (clause.length() > 0) {
            sql.append("WHERE ").append(clause.toString()).append(" ");
        }

        String sortClause = SortUtils.toSql(sort, "b");

        if (StringUtils.isNotBlank(sortClause)) {
            sql.append("ORDER BY ").append(sortClause).append(" ");
        }

        sql
                .append("LIMIT ")
                .append(limit)
                .append(" OFFSET ")
                .append(offset);

        return jdbcTemplate.query(
                sql.toString(),
                ps -> {
                    int i = 0;

                    for (PreparedSetter preparedSetter : values) {
                        preparedSetter.set(ps, ++i);
                    }
                },
                (resultSet, i) -> mapFull(resultSet, fields)
        );
    }

    public long countOff() {
        return jdbcTemplate.query("SELECT COUNT(*) FROM biography", rs -> {
            if (rs.next()) {
                return rs.getLong(1);
            }

            return (long) 0;
        });
    }

    public Biography getById(TimeZone timeZone, int id, AndCondition isLikedCriteria, List<PreparedSetter> values, Collection<String> fields) {
        StringBuilder sql = new StringBuilder();

        sql
                .append("SELECT ").append(getFullSelectList(timeZone, fields)).append(" FROM biography b ");

        appendJoins(sql, fields, isLikedCriteria);

        sql.append("WHERE b.id = ").append(id);

        return jdbcTemplate.query(
                sql.toString(),
                ps -> {
                    int i = 0;

                    for (PreparedSetter preparedSetter : values) {
                        preparedSetter.set(ps, ++i);
                    }
                },
                rs -> {
                    if (rs.next()) {
                        return mapFull(rs, fields);
                    }

                    return null;
                }
        );
    }

    public Biography getByCriteria(TimeZone timeZone,
                                   AndCondition criteria,
                                   AndCondition isLikedCriteria,
                                   List<PreparedSetter> values,
                                   Collection<String> fields) {
        StringBuilder sql = new StringBuilder();

        sql
                .append("SELECT ").append(getFullSelectList(timeZone, fields)).append(" FROM biography b ");

        appendJoins(sql, fields, isLikedCriteria);

        DslVisitor visitor = new DslVisitor("b");

        new Expression() {{
            add(criteria);
        }}.accept(visitor);

        String clause = visitor.getClause();

        sql.append("WHERE ").append(clause);

        return jdbcTemplate.query(
                sql.toString(),
                ps -> {
                    int i = 0;

                    for (PreparedSetter preparedSetter : values) {
                        preparedSetter.set(ps, ++i);
                    }
                },
                rs -> {
                    if (rs.next()) {
                        return mapFull(rs, fields);
                    }

                    return null;
                }
        );
    }

    public Collection<Biography> getFields(TimeZone timeZone, Collection<String> fields, AndCondition criteria, List<PreparedSetter> values) {
        StringBuilder sql = new StringBuilder();
        StringBuilder join = new StringBuilder();

        sql.append("SELECT ");

        if (fields.isEmpty()) {
            sql.append("*");
        } else {
            Collection<String> timeFields = Arrays.asList(Biography.CREATED_AT, Biography.UPDATED_AT);

            for (Iterator<String> fieldIterator = fields.iterator(); fieldIterator.hasNext(); ) {
                String next = fieldIterator.next();

                if (next.equals(Biography.CREATOR_ID)) {
                    sql.append("cb.id as cb_id,");
                    sql.append("cb.first_name as cb_first_name,");
                    sql.append("cb.last_name as cb_last_name");
                    join.append(" LEFT JOIN biography cb ON b.creator_id = cb.user_id ");
                } else if (timeFields.contains(next)) {
                    sql.append("b.").append(next).append("::TIMESTAMPTZ AT TIME ZONE '").append(timeZone.getID()).append(" as ").append(next);
                } else {
                    sql.append("b.").append(next);
                }

                if (fieldIterator.hasNext()) {
                    sql.append(",");
                }
            }
        }
        sql.append(" ").append("FROM biography b ");

        if (join.length() > 0) {
            sql.append(join.toString()).append(" ");
        }

        DslVisitor visitor = new DslVisitor("b");

        new Expression() {{
            add(criteria);
        }}.accept(visitor);

        String clause = visitor.getClause();

        if (StringUtils.isNotBlank(clause)) {
            sql.append("WHERE ").append(clause).append(" ");
        }

        return jdbcTemplate.query(
                sql.toString(),
                ps -> {
                    int i = 0;

                    for (PreparedSetter preparedSetter : values) {
                        preparedSetter.set(ps, ++i);
                    }
                },
                (rs, index) -> {
                    Biography biography = new Biography();

                    for (String field : fields) {
                        switch (field) {
                            case Biography.ID:
                                biography.setId(rs.getInt(Biography.ID));
                                break;
                            case Biography.FIRST_NAME:
                                biography.setFirstName(rs.getString(Biography.FIRST_NAME));
                                break;
                            case Biography.CREATOR_ID:
                                Biography creator = new Biography();

                                creator.setId(rs.getInt("cb_id"));
                                creator.setFirstName(rs.getString("cb_first_name"));
                                creator.setLastName(rs.getString("cb_last_name"));

                                biography.setCreatorId(creator.getId());
                                biography.setCreator(creator);
                                break;
                        }
                    }

                    return biography;
                }
        );
    }

    public BiographyUpdateStatus updateValues(
            TimeZone timeZone,
            Collection<UpdateValue> updateValues,
            AndCondition criteria,
            List<PreparedSetter> values
    ) {
        DslVisitor visitor = new DslVisitor(null);

        new Expression() {{
            add(criteria);
        }}.accept(visitor);

        String clause = visitor.getClause();


        StringBuilder sql = new StringBuilder();

        sql.append("UPDATE biography SET ");

        for (Iterator<UpdateValue> iterator = updateValues.iterator(); iterator.hasNext(); ) {
            sql.append(iterator.next().getName()).append(" = ?");

            if (iterator.hasNext()) {
                sql.append(", ");
            }
        }

        if (StringUtils.isNotBlank(clause)) {
            sql.append(" WHERE ").append(clause).append(" ");
        }

        sql.append("RETURNING updated_at::TIMESTAMPTZ AT TIME ZONE '").append(timeZone.getID()).append("' AS updated_at");

        return jdbcTemplate.execute(
                sql.toString(),
                (PreparedStatementCallback<BiographyUpdateStatus>) ps -> {
                    int i = 0;

                    for (UpdateValue updateValue : updateValues) {
                        updateValue.getSetter().set(ps, ++i);
                    }
                    for (PreparedSetter preparedSetter : values) {
                        preparedSetter.set(ps, ++i);
                    }

                    ps.execute();

                    try (ResultSet resultSet = ps.getResultSet()) {
                        if (resultSet.next()) {
                            return new BiographyUpdateStatus(1, resultSet.getTimestamp("updated_at"));
                        } else {
                            return new BiographyUpdateStatus(0, null);
                        }
                    }
                }
        );
    }

    public int delete(int biographyId) {
        return jdbcTemplate.update(
                "DELETE FROM biography WHERE id = ?",
                preparedStatement -> preparedStatement.setInt(1, biographyId)
        );
    }

    private String getFullSelectList(TimeZone timeZone, Collection<String> fields) {
        StringBuilder selectList = new StringBuilder();

        selectList
                .append("b.first_name,")
                .append("b.last_name,")
                .append("b.middle_name,")
                .append("b.id,")
                .append("b.creator_id,")
                .append("b.user_id,")
                .append("b.updated_at::TIMESTAMPTZ AT TIME ZONE '").append(timeZone.getID()).append("' as updated_at, ")
                .append("b.created_at::TIMESTAMPTZ AT TIME ZONE '").append(timeZone.getID()).append("' as created_at, ")
                .append("b.moderation_status,")
                .append("b.moderated_at::TIMESTAMPTZ AT TIME ZONE '").append(timeZone.getID()).append("' as moderated_at, ")
                .append("b.moderator_id,")
                .append("b.moderation_info,")
                .append("b.disable_comments,")
                .append("b.anonymous_creator,")
                .append("b.").append(Biography.BIO).append(",")
                .append("b.publish_status,")
                .append("c.name as country,")
                .append("b.country_id,")
                .append("bm.first_name as m_first_name,")
                .append("bm.last_name as m_last_name,")
                .append("bm.id as m_id,")
                .append("bm.user_id as bm_user_id,")
                .append("l.cnt as l_cnt,")
                .append("bc.cnt as bc_cnt,")
                .append("bvc.cnt as bvc_views_count");

        if (fields.contains(Biography.IS_LIKED)) {
            selectList.append(",bisl.biography_id as bisl_biography_id");
        }

        if (fields.contains(Biography.CREATOR_ID)) {
            selectList.append(",cb.id as cb_id,");
            selectList.append("cb.first_name as cb_first_name,");
            selectList.append("cb.last_name as cb_last_name,");
            selectList.append("cb.user_id as cb_user_id");
        }

        return selectList.toString();
    }

    private Biography mapFull(ResultSet rs, Collection<String> fields) throws SQLException {
        Biography biography = new Biography();

        biography.setId(rs.getInt("id"));
        biography.setFirstName(rs.getString("first_name"));
        biography.setLastName(rs.getString("last_name"));
        biography.setMiddleName(rs.getString("middle_name"));

        biography.setCreatorId(ResultSetUtils.intOrNull(rs, "creator_id"));
        biography.setUserId(ResultSetUtils.intOrNull(rs, "user_id"));
        biography.setUpdatedAt(rs.getTimestamp("updated_at"));
        biography.setCreatedAt(rs.getTimestamp("created_at"));
        biography.setModerationStatus(Biography.ModerationStatus.fromCode(rs.getInt("moderation_status")));
        biography.setModeratedAt(rs.getTimestamp("moderated_at"));

        biography.setModeratorId(ResultSetUtils.intOrNull(rs, "moderator_id"));
        biography.setBio(rs.getString(Biography.BIO));
        biography.setModerationInfo(rs.getString("moderation_info"));
        biography.setPublishStatus(Biography.PublishStatus.fromCode(ResultSetUtils.intOrNull(rs, "publish_status")));

        if (biography.getModeratorId() != null) {
            Biography moderatorBiography = new Biography();

            moderatorBiography.setId(rs.getInt("m_id"));
            moderatorBiography.setFirstName(rs.getString("m_first_name"));
            moderatorBiography.setLastName(rs.getString("m_last_name"));
            moderatorBiography.setUserId(biography.getModeratorId());

            biography.setModerator(moderatorBiography);
        }

        if (fields.contains(Biography.IS_LIKED)) {
            rs.getInt("bisl_biography_id");

            biography.setLiked(!rs.wasNull());
        }

        biography.setLikesCount(rs.getInt("l_cnt"));
        biography.setCommentsCount(rs.getInt("bc_cnt"));
        biography.setViewsCount(rs.getLong("bvc_views_count"));

        biography.setCountryId(ResultSetUtils.intOrNull(rs, Biography.COUNTRY_ID));

        if (biography.getCountryId() != null) {
            Country country = new Country();

            country.setId(biography.getCountryId());
            country.setName(rs.getString("country"));

            biography.setCountry(country);
        }

        boolean anonymous = rs.getBoolean("anonymous_creator");

        biography.setAnonymousCreator(anonymous);
        if (!anonymous && fields.contains(Biography.CREATOR_ID)) {
            Biography creator = new Biography();

            creator.setId(rs.getInt("cb_id"));
            creator.setFirstName(rs.getString("cb_first_name"));
            creator.setLastName(rs.getString("cb_last_name"));
            creator.setUserId(rs.getInt("cb_user_id"));

            biography.setCreator(creator);
        }

        boolean deleteComments = rs.getBoolean("disable_comments");

        biography.setDisableComments(deleteComments);

        return biography;
    }

    private void appendJoins(StringBuilder sql, Collection<String> fields, AndCondition isLikedCriteria) {
        sql.append(" LEFT JOIN biography bm ON b.moderator_id = bm.user_id ")
                .append(" LEFT JOIN (SELECT biography_id, COUNT(id) AS cnt FROM biography_like GROUP BY biography_id) l ON b.id = l.biography_id ")
                .append(" LEFT JOIN (SELECT biography_id, COUNT(id) AS cnt FROM biography_comment GROUP BY biography_id) bc ON b.id = bc.biography_id ")
                .append(" LEFT JOIN (SELECT biography_id, views_count as cnt FROM biography_view_count) bvc ON b.id = bvc.biography_id ")
                .append(" LEFT JOIN country c ON b.country_id = c.id ");

        if (fields.contains(Biography.IS_LIKED)) {
            DslVisitor visitor = new DslVisitor(null);

            new Expression() {{
                add(isLikedCriteria);
            }}.accept(visitor);

            String isLikedClause = visitor.getClause();

            sql
                    .append(" LEFT JOIN (SELECT biography_id FROM biography_like ");

            if (StringUtils.isNotBlank(isLikedClause)) {
                sql.append("WHERE ").append(isLikedClause);
            }

            sql.append(") bisl ON b.id = bisl.biography_id ");
        }

        if (fields.contains(Biography.CREATOR_ID)) {
            sql.append(" LEFT JOIN biography cb ON b.creator_id = cb.user_id ");
        }
    }
}
