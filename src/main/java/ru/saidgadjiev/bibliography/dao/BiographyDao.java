package ru.saidgadjiev.bibliography.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliography.domain.Biography;

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
                        Biography biography = new Biography(
                                rs.getString("first_name"),
                                rs.getString("last_name"),
                                rs.getString("middle_name"),
                                rs.getString("creator_name"),
                                rs.getString("user_name")
                        );

                        biography.setBiography(rs.getString("biography"));

                        return biography;
                    }

                    return null;
                }
        );
    }
}
