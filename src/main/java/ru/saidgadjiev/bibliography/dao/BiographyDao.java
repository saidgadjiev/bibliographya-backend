package ru.saidgadjiev.bibliography.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.BiographyUpdateStatus;
import ru.saidgadjiev.bibliography.model.UpdateBiographyRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public void save(Biography biography) {
        String firstName = biography.getFirstName() == null ? "NULL" : "'" + biography.getFirstName() + "'";
        String lastName = biography.getLastName() == null ? "NULL" : "'" + biography.getLastName() + "'";
        String middleName = biography.getMiddleName() == null ? "NULL" : "'" + biography.getMiddleName() + "'";
        String userName = biography.getUserName() == null ? "NULL" : "'" + biography.getUserName() + "'";
        String creatorName = biography.getCreatorName() == null ? "NULL" : "'" + biography.getCreatorName() + "'";

        jdbcTemplate.execute("INSERT INTO biography" +
                "(\"first_name\", \"last_name\", \"middle_name\", \"creator_name\", \"user_name\") " +
                "VALUES" +
                "("
                + firstName + ", "
                + lastName + ", "
                + middleName + ", "
                + creatorName + ", "
                + userName + "" +
                ")");
    }

    public Biography getByUsername(String username) {
        return jdbcTemplate.query(
                "SELECT * FROM biography WHERE user_name='" + username + "'",
                rs -> {
                    if (rs.next()) {
                        return map(rs);
                    }

                    return null;
                }
        );
    }

    public List<Biography> getBiographiesList(int limit, long offset) {
        return jdbcTemplate.query(
                "SELECT * FROM biography LIMIT " + limit + " OFFSET " + offset,
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
}
