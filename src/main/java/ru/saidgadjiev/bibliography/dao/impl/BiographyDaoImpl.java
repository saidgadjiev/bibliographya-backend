package ru.saidgadjiev.bibliography.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliography.dao.api.BiographyDao;
import ru.saidgadjiev.bibliography.data.FilterCriteria;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.BiographyUpdateStatus;
import ru.saidgadjiev.bibliography.utils.ResultSetUtils;
import ru.saidgadjiev.bibliography.utils.SortUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static ru.saidgadjiev.bibliography.utils.FilterUtils.toClause;

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
                        "(first_name, last_name, middle_name, biography, creator_id, user_id) " +
                        "VALUES(?, ?, ?, ?, ?, ?) " +
                        "RETURNING *",
                (PreparedStatementCallback<Biography>) ps -> {
                    ps.setString(1, biography.getFirstName());
                    ps.setString(2, biography.getLastName());
                    ps.setString(3, biography.getMiddleName());
                    ps.setString(4, biography.getBiography());
                    ps.setInt(5, biography.getCreatorId());
                    ps.setInt(6, biography.getUserId());

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

        if (categoryName != null) {
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

        biography.setCreatorId(ResultSetUtils.intOrNull(rs,"creator_id"));
        biography.setUserId(ResultSetUtils.intOrNull(rs,"user_id"));
        biography.setUpdatedAt(rs.getTimestamp("updated_at"));
        biography.setModerationStatus(Biography.ModerationStatus.fromCode(rs.getInt("moderation_status")));
        biography.setModeratedAt(rs.getTimestamp("moderated_at"));

        biography .setModeratorId(ResultSetUtils.intOrNull(rs,"moderator_id"));
        biography.setBiography(rs.getString("biography"));
        biography.setModerationInfo(rs.getString("moderation_info"));

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
    public BiographyUpdateStatus update(Biography biography) throws SQLException {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "UPDATE biography " +
                            "SET first_name=?, last_name=?, middle_name=?, biography=? " +
                            "WHERE id=? AND updated_at=? RETURNING updated_at")) {
                ps.setString(1, biography.getFirstName());
                ps.setString(2, biography.getLastName());
                ps.setString(3, biography.getMiddleName());
                ps.setString(4, biography.getBiography());
                ps.setInt(5, biography.getId());
                ps.setTimestamp(6, biography.getUpdatedAt());

                ps.execute();

                try (ResultSet resultSet = ps.getResultSet()) {
                    if (resultSet.next()) {
                        return new BiographyUpdateStatus(true, resultSet.getTimestamp("updated_at"));
                    } else {
                        return new BiographyUpdateStatus(false, null);
                    }
                }
            }
        }
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
        selectList.append("bm.first_name as m_first_name,");
        selectList.append("bm.last_name as m_last_name,");
        selectList.append("bm.id as m_id");

        return selectList.toString();
    }
}
