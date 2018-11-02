package ru.saidgadjiev.bibliography.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliography.domain.Biography;

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
        Biography biography = new Biography(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("middle_name"),
                rs.getString("creator_name"),
                rs.getString("user_name")
        );

        biography.setBiography(rs.getString("biography"));

        return biography;
    }
}
