package ru.saidgadjiev.bibliography.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
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
import java.util.List;

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
                        return map(resultSet);
                    }

                    return null;
                }

            }
        }
    }

    public Biography getBiography(Collection<FilterCriteria> biographyCriteria) {
        String clause = toClause(biographyCriteria);

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
                        return map(rs);
                    }

                    return null;
                }
        );
    }

    public List<Biography> getBiographiesList(int limit,
                                              long offset,
                                              Collection<FilterCriteria> biographyCriteria,
                                              String categoryName
    ) {
        String clause = toClause(biographyCriteria);
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT * FROM biography b LEFT JOIN biography_category_biography bc ON b.id = bc.biography_id WHERE bc.category_name = ? ");

        if (clause != null) {
            sql.append(clause).append(" ");
        }

        sql.append("LIMIT ").append(limit).append(" OFFSET ").append(offset);

        return jdbcTemplate.query(
                sql.toString(),
                ps -> {
                    ps.setString(1, categoryName);

                    int i = 1;

                    for (FilterCriteria criterion : biographyCriteria) {
                        criterion.getValueSetter().set(ps, ++i, criterion.getFilterValue());
                    }
                },
                (resultSet, i) -> map(resultSet)
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

    private Biography map(ResultSet rs) throws SQLException {
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

        return biography;
    }

    public Biography getById(int id) {
        return jdbcTemplate.query(
                "SELECT * FROM biography WHERE id=" + id + "",
                rs -> {
                    if (rs.next()) {
                        return map(rs);
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

    private String toClause(Collection<FilterCriteria> criteria) {
        StringBuilder clause = new StringBuilder();

        if (!criteria.isEmpty()) {
            for (FilterCriteria criterion : criteria) {
                switch (criterion.getFilterOperation()) {
                    case EQ:
                        clause.append(criterion.getPropertyName()).append("=").append("?");

                        break;
                }
            }

            return clause.toString();
        }

        return null;
    }
}
