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
import ru.saidgadjiev.bibliographya.utils.FilterUtils;
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
                        "(first_name, last_name, middle_name, biography, creator_id, user_id, publish_status, is_autobiography) " +
                        "VALUES(?, ?, ?, ?, ?, ?, ?, ?) " +
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

                    if (biography.getPublishStatus() == null) {
                        ps.setNull(7, Types.INTEGER);
                    } else {
                        ps.setInt(7, biography.getPublishStatus().getCode());
                    }
                    ps.setBoolean(8, biography.getIsAutobiography());

                    ps.execute();

                    try (ResultSet resultSet = ps.getResultSet()) {
                        if (resultSet.next()) {
                            return mapFull(resultSet);
                        }

                        return null;
                    }
                }
        );
    }

    @Override
    public Biography getBiography(Collection<FilterCriteria> biographyCriteria) {
        String clause = toClause(biographyCriteria, null);

        return jdbcTemplate.query(
                "SELECT * FROM biography " + (clause.length() > 0 ? "WHERE " + clause : ""),
                ps -> {
                    int i = 0;

                    for (FilterCriteria criterion : biographyCriteria) {
                        criterion.getValueSetter().set(ps, ++i, criterion.getFilterValue());
                    }
                },
                rs -> {
                    if (rs.next()) {
                        return mapFull(rs);
                    }

                    return null;
                }
        );
    }

    @Override
    public List<Biography> getBiographiesList(int limit,
                                              long offset,
                                              String categoryName,
                                              Collection<FilterCriteria> biographyCriteria,
                                              Sort sort
    ) {
        StringBuilder clause = new StringBuilder();

        if (StringUtils.isNotBlank(categoryName)) {
            clause
                    .append("b.id IN (SELECT biography_id FROM biography_category_biography WHERE category_name = '")
                    .append(categoryName)
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
                .append(" FROM biography b LEFT JOIN biography bm ON b.moderator_id = bm.user_id ");

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
                (resultSet, i) -> mapFull(resultSet)
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

    private Biography mapFull(ResultSet rs) throws SQLException {
        Biography biography = new Biography();

        biography.setId(rs.getInt("id"));
        biography.setFirstName(rs.getString("first_name"));
        biography.setLastName(rs.getString("last_name"));
        biography.setMiddleName(rs.getString("middle_name"));
        biography.setIsAutobiography(rs.getBoolean("is_autobiography"));

        biography.setCreatorId(ResultSetUtils.intOrNull(rs,"creator_id"));
        biography.setUserId(ResultSetUtils.intOrNull(rs,"user_id"));
        biography.setUpdatedAt(rs.getTimestamp("updated_at"));
        biography.setModerationStatus(Biography.ModerationStatus.fromCode(rs.getInt("moderation_status")));
        biography.setModeratedAt(rs.getTimestamp("moderated_at"));

        biography .setModeratorId(ResultSetUtils.intOrNull(rs,"moderator_id"));
        biography.setBiography(rs.getString("biography"));
        biography.setModerationInfo(rs.getString("moderation_info"));
        biography.setPublishStatus(Biography.PublishStatus.fromCode(ResultSetUtils.intOrNull(rs, "publish_status")));

        if (biography.getModeratorId() != null) {
            Biography moderatorBiography = new Biography();

            moderatorBiography.setId(rs.getInt("m_id"));
            moderatorBiography.setFirstName(rs.getString("m_first_name"));
            moderatorBiography.setLastName(rs.getString("m_last_name"));
            moderatorBiography.setUserId(biography.getModeratorId());

            biography.setModeratorBiography(moderatorBiography);
        }

        return biography;
    }

    @Override
    public Biography getById(int id) {
        return jdbcTemplate.query(
                "SELECT " + getFullSelectList() + " FROM biography b LEFT JOIN biography bm ON b.moderator_id = bm.user_id  WHERE b.id=" + id + "",
                rs -> {
                    if (rs.next()) {
                        return mapFull(rs);
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
                        if (updateValue.isNeedPreparedSet()) {
                            updateValue.getSetter().set(ps, ++i, updateValue.getValue());
                        }
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

    @Override
    public List<Map<String, Object>> getFields(Collection<String> fields, Collection<FilterCriteria> criteria) {
        StringBuilder builder = new StringBuilder();

        builder.append("SELECT ");

        if (fields.isEmpty()) {
            builder.append("*");
        } else {
            for (Iterator<String> fieldIterator = fields.iterator(); fieldIterator.hasNext(); ) {
                builder.append(fieldIterator.next());

                if (fieldIterator.hasNext()) {
                    builder.append(",");
                }
            }
        }
        builder.append(" ").append("FROM biography ");

        String clause = FilterUtils.toClause(criteria, null);

        if (StringUtils.isNotBlank(clause)) {
            builder.append("WHERE ").append(clause);
        }

        return jdbcTemplate.query(
                builder.toString(),
                ps -> {
                    int i = 0;

                    for (FilterCriteria criterion : criteria) {
                        if (criterion.isNeedPreparedSet()) {
                            criterion.getValueSetter().set(ps, ++i, criterion.getFilterValue());
                        }
                    }
                },
                rs -> {
                    List<Map<String, Object>> result = new ArrayList<>();
                    ResultSetMetaData md = rs.getMetaData();
                    int columns = md.getColumnCount();

                    while (rs.next()){
                        Map<String, Object> row = new HashMap<>(columns);

                        for(int i=1; i<=columns; ++i){
                            row.put(md.getColumnName(i),rs.getObject(i));
                        }
                        result.add(row);
                    }

                    return result;
                }
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
        selectList.append("b.is_autobiography,");
        selectList.append("bm.first_name as m_first_name,");
        selectList.append("bm.last_name as m_last_name,");
        selectList.append("bm.id as m_id");

        return selectList.toString();
    }
}
