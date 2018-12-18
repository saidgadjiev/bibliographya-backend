package ru.saidgadjiev.bibliography.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliography.data.FilterCriteria;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.BiographyUpdateStatus;
import ru.saidgadjiev.bibliography.model.ModerationStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.saidgadjiev.bibliography.data.FilterUtils.toClause;

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

    public Biography save(Biography biography) throws SQLException {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO biography" +
                            "(\"first_name\", \"last_name\", \"middle_name\", \"biography\", \"creator_name\", \"user_name\") " +
                            "VALUES(?, ?, ?, ?, ?, ?) " +
                            "RETURNING id, first_name, last_name, middle_name, biography, creator_name, user_name, updated_at"
            )) {
                ps.setString(1, biography.getFirstName());
                ps.setString(2, biography.getLastName());
                ps.setString(3, biography.getMiddleName());
                ps.setString(4, biography.getBiography());
                ps.setString(5, biography.getCreatorName());
                ps.setString(6, biography.getUserName());

                ps.execute();

                try (ResultSet resultSet = ps.getResultSet()) {
                    if (resultSet.next()) {
                        return mapFull(resultSet);
                    }

                    return null;
                }

            }
        }
    }

    public Biography getBiography(Collection<FilterCriteria> biographyCriteria) {
        String clause = toClause(biographyCriteria, null);

        return jdbcTemplate.query(
                "SELECT * FROM biography " + (clause != null ? "WHERE " + clause : ""),
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

    public List<Biography> getBiographiesList(int limit,
                                              long offset,
                                              String categoryName,
                                              Collection<FilterCriteria> biographyCriteria
    ) {
        StringBuilder clause = new StringBuilder();

        if (categoryName != null) {
            clause
                    .append("b.id IN (SELECT biography_id FROM biography_category_biography WHERE category_name = '")
                    .append(categoryName)
                    .append("')");
        }

        String biographyClause = toClause(biographyCriteria, "b");

        if (biographyClause != null) {
            if (clause.length() > 0) {
                clause.append(" AND ");
            }
            clause.append(biographyClause);
        }
        StringBuilder sql = new StringBuilder();

        sql
                .append("SELECT ")
                .append(getFullSelectList())
                .append(" FROM biography b LEFT JOIN biography bm ON b.moderator_name = bm.user_name ");
        if (clause.length() > 0) {
            sql.append("WHERE ").append(clause.toString()).append(" ");
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

    public long countOff() {
        return jdbcTemplate.query("SELECT COUNT(*) FROM biography", rs -> {
            if (rs.next()) {
                return rs.getLong(1);
            }

            return (long) 0;
        });
    }

    private Biography mapFull(ResultSet rs) throws SQLException {
        Biography biography = new Biography.Builder(
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("middle_name")
        )
                .setId(rs.getInt("id"))
                .setCreatorName(rs.getString("creator_name"))
                .setUserName(rs.getString("user_name"))
                .setUpdatedAt(rs.getTimestamp("updated_at"))
                .setModerationStatus(ModerationStatus.fromCode(rs.getInt("moderation_status")))
                .setModeratedAt(rs.getTimestamp("moderated_at"))
                .setModeratorName(rs.getString("moderator_name"))
                .build();

        biography.setBiography(rs.getString("biography"));

        if (biography.getModeratorName() != null) {
            Biography moderatorBiography = new Biography.Builder()
                    .setFirstName(rs.getString("m_first_name"))
                    .setLastName(rs.getString("m_last_name"))
                    .setId(rs.getInt("m_id"))
                    .setUserName(biography.getModeratorName())
                    .build();

            biography.setModeratorBiography(moderatorBiography);
        }

        return biography;
    }

    public Biography getById(int id) {
        return jdbcTemplate.query(
                "SELECT " + getFullSelectList() + " FROM biography b LEFT JOIN biography bm ON b.moderator_name = bm.user_name  WHERE b.id=" + id + "",
                rs -> {
                    if (rs.next()) {
                        return mapFull(rs);
                    }

                    return null;
                }
        );
    }

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

    private String getFullSelectList() {
        StringBuilder selectList = new StringBuilder();

        selectList.append("b.first_name,");
        selectList.append("b.last_name,");
        selectList.append("b.middle_name,");
        selectList.append("b.id,");
        selectList.append("b.creator_name,");
        selectList.append("b.user_name,");
        selectList.append("b.updated_at,");
        selectList.append("b.moderation_status,");
        selectList.append("b.moderated_at,");
        selectList.append("b.moderator_name,");
        selectList.append("b.biography,");
        selectList.append("bm.first_name as m_first_name,");
        selectList.append("bm.last_name as m_last_name,");
        selectList.append("bm.id as m_id");

        return selectList.toString();
    }
}
