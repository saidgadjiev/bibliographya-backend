package ru.saidgadjiev.bibliographya.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliographya.domain.EmailVerification;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by said on 11.02.2019.
 */
@Repository
public class EmailVerificationDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public EmailVerificationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(EmailVerification emailVerification) {
        jdbcTemplate.update(
                "INSERT INTO email_verification(email, code, expired_at) VALUES(?, ?, ?)",
                preparedStatement -> {
                    preparedStatement.setString(1, emailVerification.getEmail());
                    preparedStatement.setInt(2, emailVerification.getCode());
                    preparedStatement.setTimestamp(3, emailVerification.getExpiredAt());
                }
        );
    }

    public EmailVerification getByEmail(String email) {
        return jdbcTemplate.query(
                "SELECT * FROM email_verification WHERE email = ?",
                preparedStatement -> preparedStatement.setString(1, email),
                resultSet -> {
                    if (resultSet.next()) {
                        EmailVerification emailVerification = new EmailVerification();

                        emailVerification.setId(resultSet.getInt("id"));
                        emailVerification.setEmail(resultSet.getString("email"));
                        emailVerification.setCode(resultSet.getInt("code"));
                        emailVerification.setExpiredAt(resultSet.getTimestamp("expired_at"));

                        return emailVerification;
                    }
                    return null;
                }
        );
    }

    public int updateCode(String email, int code) {
        return jdbcTemplate.update(
                "UPDATE email_verification SET code = ? WHERE email = ?",
                preparedStatement -> {
                    preparedStatement.setInt(1, code);
                    preparedStatement.setString(2, email);
                }
        );
    }

    public int deleteByEmail(String email) {
        return jdbcTemplate.update(
                "DELETE FROM email_verification WHERE email = ?",
                preparedStatement -> preparedStatement.setString(1, email)
        );
    }
}
