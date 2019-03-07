package ru.saidgadjiev.bibliographya.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliographya.dao.api.BiographyDao;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.BiographyUpdateStatus;
import ru.saidgadjiev.bibliographya.utils.ResultSetUtils;
import ru.saidgadjiev.bibliographya.utils.SortUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.*;
import java.util.stream.Collectors;

import static ru.saidgadjiev.bibliographya.utils.FilterUtils.toClause;

/**
 * Created by said on 22.10.2018.
 */
@Repository
@Qualifier("sql")
@SuppressWarnings("CPD-START")
public class BiographyDaoImpl implements BiographyDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public BiographyDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Biography save(Biography biography) throws SQLException {
        return jdbcTemplate.execute(
                "INSERT INTO biography" +
                        "(first_name, last_name, middle_name, biography, creator_id, user_id) " +
                        "VALUES(?, ?, ?, ?, ?, ?) " +
                        "RETURNING *",
                (PreparedStatementCallback<Biography>) ps -> {
                    ps.setString(1, biography.getFirstName());
                    ps.setString(2, biography.getLastName());

                    if (StringUtils.isBlank(biography.getMiddleName())) {
                        ps.setNull(3, Types.VARCHAR);
                    } else {
                        ps.setString(3, biography.getMiddleName());
                    }
                    if (StringUtils.isBlank(biography.getBiography())) {
                        ps.setNull(4, Types.VARCHAR);
                    } else {
                        ps.setString(4, biography.getBiography());
                    }

                    ps.setInt(5, biography.getCreatorId());

                    if (biography.getUserId() == null) {
                        ps.setNull(6, Types.INTEGER);
                    } else {
                        ps.setInt(6, biography.getUserId());
                    }

                    ps.execute();

                    try (ResultSet resultSet = ps.getResultSet()) {
                        if (resultSet.next()) {
                            return mapFull(resultSet, false, false);
                        }

                        return null;
                    }
                }
        );
    }

    @Override
    public Biography save(Collection<UpdateValue> values) throws SQLException {
        StringBuilder sql = new StringBuilder();

        sql.append("INSERT INTO biography(");

        StringBuilder valuesBuilder = new StringBuilder();

        for (Iterator<UpdateValue> iterator = values.iterator(); iterator.hasNext(); ) {
            sql.append(iterator.next().getName());
            valuesBuilder.append("?");

            if (iterator.hasNext()) {
                sql.append(", ");
                valuesBuilder.append(",");
            }
        }

        sql.append(") VALUES(");

        sql.append(valuesBuilder.toString());

        sql.append(") ");

        sql.append("RETURNING *");

        return jdbcTemplate.execute(
                sql.toString(),
                (PreparedStatementCallback<Biography>) ps -> {
                    int i = 0;

                    for (UpdateValue updateValue : values) {
                        updateValue.getSetter().set(ps, ++i, updateValue.getValue());
                    }

                    ps.execute();

                    try (ResultSet resultSet = ps.getResultSet()) {
                        if (resultSet.next()) {
                            return mapFull(resultSet, false, false);
                        }

                        return null;
                    }
                }
        );
    }

    @Override
    public List<Biography> getBiographiesList(int limit,
                                              long offset,
                                              Integer categoryId,
                                              Collection<FilterCriteria> biographyCriteria,
                                              Sort sort
    ) {
        StringBuilder clause = new StringBuilder();

        if (categoryId != null) {
            clause
                    .append("b.id IN (SELECT biography_id FROM biography_category_biography WHERE category_id = '")
                    .append(categoryId)
                    .append("')");
        }

        String biographyClause = toClause(biographyCriteria, "b");

        if (biographyClause.length() > 0) {
            if (clause.length() > 0) {
                clause.append(" AND ");
            }
            clause.append(biographyClause);
        }
        StringBuilder sql = new StringBuilder();

        sql
                .append("SELECT ")
                .append(getFullSelectList())
                .append(" FROM biography b")
                .append(" LEFT JOIN biography bm ON b.moderator_id = bm.user_id ")
                .append(" LEFT JOIN biography cb ON b.creator_id = cb.user_id ");

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

                    for (FilterCriteria criterion
                            : biographyCriteria
                            .stream()
                            .filter(FilterCriteria::isNeedPreparedSet)
                            .collect(Collectors.toList())
                            ) {
                        criterion.getValueSetter().set(ps, ++i, criterion.getFilterValue());
                    }
                },
                (resultSet, i) -> mapFull(resultSet, true, true)
        );
    }

    @Override
    public long countOff() {
        return jdbcTemplate.query("SELECT COUNT(*) FROM biography", rs -> {
            if (rs.next()) {
                return rs.getLong(1);
            }

            return (long) 0;
        });
    }

    @Override
    public Biography getById(int id) {
        return jdbcTemplate.query(
                "SELECT " + getFullSelectList() + " FROM biography b \n" +
                        "  LEFT JOIN biography bm ON b.moderator_id = bm.user_id\n" +
                        "  LEFT JOIN biography cb ON b.creator_id = cb.user_id\n" +
                        "WHERE b.id=" + id + "",
                rs -> {
                    if (rs.next()) {
                        return mapFull(rs, true, true);
                    }

                    return null;
                }
        );
    }

    @Override
    public BiographyUpdateStatus updateValues(Collection<UpdateValue> updateValues, Collection<FilterCriteria> criteria) {
        String clause = toClause(criteria, null);

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

        sql.append("RETURNING updated_at");

        return jdbcTemplate.execute(
                sql.toString(),
                (PreparedStatementCallback<BiographyUpdateStatus>) ps -> {
                    int i = 0;

                    for (UpdateValue updateValue : updateValues) {
                        updateValue.getSetter().set(ps, ++i, updateValue.getValue());
                    }
                    for (FilterCriteria criterion : criteria) {
                        if (criterion.isNeedPreparedSet()) {
                            criterion.getValueSetter().set(ps, ++i, criterion.getFilterValue());
                        }
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

    @Override
    public int delete(int biographyId) {
        return jdbcTemplate.update(
                "DELETE FROM biography WHERE id = ?",
                preparedStatement -> preparedStatement.setInt(1, biographyId)
        );
    }

    private String getFullSelectList() {
        StringBuilder selectList = new StringBuilder();

        selectList.append("b.first_name,");
        selectList.append("b.last_name,");
        selectList.append("b.middle_name,");
        selectList.append("b.id,");
        selectList.append("b.creator_id,");
        selectList.append("b.user_id,");
        selectList.append("b.updated_at,");
        selectList.append("b.moderation_status,");
        selectList.append("b.moderated_at,");
        selectList.append("b.moderator_id,");
        selectList.append("b.moderation_info,");
        selectList.append("b.biography,");
        selectList.append("b.publish_status,");
        selectList.append("bm.first_name as m_first_name,");
        selectList.append("bm.last_name as m_last_name,");
        selectList.append("bm.id as m_id,");
        selectList.append("cb.id as cb_id,");
        selectList.append("cb.first_name as cb_first_name,");
        selectList.append("cb.last_name as cb_last_name");

        return selectList.toString();
    }

    private Biography mapFull(ResultSet rs, boolean mapCreator, boolean mapModerator) throws SQLException {
        Biography biography = new Biography();

        biography.setId(rs.getInt("id"));
        biography.setFirstName(rs.getString("first_name"));
        biography.setLastName(rs.getString("last_name"));
        biography.setMiddleName(rs.getString("middle_name"));

        biography.setCreatorId(ResultSetUtils.intOrNull(rs, "creator_id"));
        biography.setUserId(ResultSetUtils.intOrNull(rs, "user_id"));
        biography.setUpdatedAt(rs.getTimestamp("updated_at"));
        biography.setModerationStatus(Biography.ModerationStatus.fromCode(rs.getInt("moderation_status")));
        biography.setModeratedAt(rs.getTimestamp("moderated_at"));

        biography.setModeratorId(ResultSetUtils.intOrNull(rs, "moderator_id"));
        biography.setBiography(rs.getString("biography"));
        biography.setModerationInfo(rs.getString("moderation_info"));
        biography.setPublishStatus(Biography.PublishStatus.fromCode(ResultSetUtils.intOrNull(rs, "publish_status")));

        if (biography.getModeratorId() != null && mapModerator) {
            Biography moderatorBiography = new Biography();

            moderatorBiography.setId(rs.getInt("m_id"));
            moderatorBiography.setFirstName(rs.getString("m_first_name"));
            moderatorBiography.setLastName(rs.getString("m_last_name"));
            moderatorBiography.setUserId(biography.getModeratorId());

            biography.setModerator(moderatorBiography);
        }

        if (mapCreator) {
            Biography creator = new Biography();

            creator.setId(rs.getInt("cb_id"));
            creator.setFirstName(rs.getString("cb_first_name"));
            creator.setLastName(rs.getString("cb_last_name"));

            biography.setCreator(creator);
        }

        return biography;
    }
}
