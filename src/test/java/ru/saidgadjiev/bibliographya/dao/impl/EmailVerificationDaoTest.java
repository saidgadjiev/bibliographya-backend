package ru.saidgadjiev.bibliographya.dao.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.saidgadjiev.bibliographya.configuration.BibliographyaTestConfiguration;
import ru.saidgadjiev.bibliographya.domain.EmailVerification;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class EmailVerificationDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EmailVerificationDao emailVerificationDao;

    @BeforeEach
    void init() {
        createTable();
    }

    @AfterEach
    void after() {
        deleteTable();
    }

    @Test
    void create() {
        EmailVerification emailVerification = new EmailVerification();

        emailVerification.setCode(1024);
        emailVerification.setEmail("Test");
        emailVerification.setExpiredAt(Timestamp.valueOf(LocalDateTime.now()));

        emailVerificationDao.create(emailVerification);

        EmailVerification actual = jdbcTemplate.query(
                "SELECT * FROM email_verification WHERE id = 1",
                resultSet -> {
                    if (resultSet.next()) {
                        EmailVerification result = new EmailVerification();

                        result.setId(resultSet.getInt("id"));
                        result.setEmail(resultSet.getString("email"));
                        result.setCode(resultSet.getInt("code"));
                        result.setExpiredAt(resultSet.getTimestamp("expired_at"));

                        return result;
                    }

                    return null;
                }
        );

        Assertions.assertNotNull(actual);
        emailVerification.setId(1);

        assertEquals(emailVerification, actual);
    }

    @Test
    void getByEmail() {
        jdbcTemplate.update(
                "INSERT INTO email_verification(email, code, expired_at) VALUES('test', 1024, {ts '1995-07-01 00:00:00'})"
        );

        EmailVerification actual = emailVerificationDao.getByEmail("test");

        Assertions.assertNotNull(actual);
        EmailVerification expected = new EmailVerification();

        expected.setId(1);
        expected.setCode(1024);
        expected.setEmail("test");

        expected.setExpiredAt(Timestamp.valueOf(LocalDateTime.of(1995, 7, 1, 0, 0, 0)));

        assertEquals(expected, actual);
    }

    @Test
    void updateCode() {
        jdbcTemplate.update(
                "INSERT INTO email_verification(email, code, expired_at) VALUES('test', 1024, {ts '1995-07-01 00:00:00'})"
        );

        EmailVerification actual = emailVerificationDao.getByEmail("test");

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(1024, actual.getCode());

        emailVerificationDao.updateCode("test", 1025);

        EmailVerification actualUpdated = emailVerificationDao.getByEmail("test");

        Assertions.assertEquals(1025, actualUpdated.getCode());
    }

    @Test
    void deleteByEmail() {
        jdbcTemplate.update(
                "INSERT INTO email_verification(email, code, expired_at) VALUES('test', 1024, {ts '1995-07-01 00:00:00'})"
        );

        int delete = emailVerificationDao.deleteByEmail("test");

        Assertions.assertEquals(1, delete);

        Object result = jdbcTemplate.query(
                "SELECT * FROM email_verification WHERE id = 1",
                resultSet -> {
                    if (resultSet.next()) {
                        return new Object();
                    }

                    return null;
                }
        );

        Assertions.assertNull(result);
    }

    private void assertEquals(EmailVerification expected, EmailVerification actual) {
        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.getCode(), actual.getCode());
        Assertions.assertEquals(expected.getEmail(), actual.getEmail());
        Assertions.assertEquals(expected.getExpiredAt(), actual.getExpiredAt());
    }

    private void createTable() {
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS email_verification (\n" +
                        "  id SERIAL PRIMARY KEY,\n" +
                        "  email VARCHAR(512) UNIQUE NOT NULL,\n" +
                        "  code INTEGER NOT NULL,\n" +
                        "  expired_at TIMESTAMP NOT NULL\n" +
                        ")"
        );
    }

    private void deleteTable() {
        jdbcTemplate.execute(
                "DROP TABLE IF EXISTS email_verification"
        );
    }
}