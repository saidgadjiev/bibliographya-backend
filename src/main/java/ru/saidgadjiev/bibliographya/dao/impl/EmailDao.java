package ru.saidgadjiev.bibliographya.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliographya.domain.Email;

/**
 * Created by said on 03.03.2019.
 */
@Repository
public class EmailDao {

    private JdbcTemplate jdbcTemplate;

    public EmailDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(Email email) {
        jdbcTemplate.update(
                "INSERT INTO email(email, verified) VALUES(?, ?)",
                preparedStatement -> {
                    preparedStatement.setString(1, email.getEmail());
                    preparedStatement.setBoolean(2, email.isVerified());
                }
        );
    }

    public void updateVerify(String email, boolean verified) {
        jdbcTemplate.update(
                "UPDATE email SET verified = ? WHERE email = ?",
                preparedStatement -> {
                    preparedStatement.setBoolean(1, verified);
                    preparedStatement.setString(2, email);
                }
        );
    }
}
