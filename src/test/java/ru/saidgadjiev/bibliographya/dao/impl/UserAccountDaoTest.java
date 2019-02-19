package ru.saidgadjiev.bibliographya.dao.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.domain.UserAccount;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class UserAccountDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserAccountDao userAccountDao;

    @BeforeEach
    void init() {
        createTables();
    }

    @AfterEach
    void after() {
        deleteTables();
    }

    @Test
    void save() {
        UserAccount userAccount = new UserAccount();

        userAccount.setEmail("test");
        userAccount.setPassword("test");

        User user = new User();

        user.setProviderType(ProviderType.EMAIL_PASSWORD);
        user.setUserAccount(userAccount);

        User result = userAccountDao.save(user);

        Assertions.assertEquals(result.getUserAccount().getId(), 1);
        Assertions.assertEquals(result.getId(), 1);

        Assertions.assertNull(userAccountDao.getByUserId(user.getId()));

        createUserBiography();

        assertEquals(result, userAccountDao.getByUserId(user.getId()));
    }

    @Test
    void getByUsername() {
        UserAccount userAccount = new UserAccount();

        userAccount.setEmail("test");
        userAccount.setPassword("test");

        User user = new User();

        user.setProviderType(ProviderType.EMAIL_PASSWORD);
        user.setUserAccount(userAccount);

        User result = userAccountDao.save(user);

        Assertions.assertEquals(result.getUserAccount().getId(), 1);
        Assertions.assertEquals(result.getId(), 1);

        Assertions.assertNull(userAccountDao.getByEmail("test"));

        createUserBiography();

        assertEquals(result, userAccountDao.getByEmail("test"));
    }

    @Test
    void isExistUsername() {
        UserAccount userAccount = new UserAccount();

        userAccount.setEmail("test");
        userAccount.setPassword("test");

        User user = new User();

        user.setProviderType(ProviderType.EMAIL_PASSWORD);
        user.setUserAccount(userAccount);

        Assertions.assertFalse(userAccountDao.isExistEmail("test"));

        userAccountDao.save(user);

        Assertions.assertTrue(userAccountDao.isExistEmail("test"));
    }

    private void assertEquals(User expected, User actual) {
        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.getProviderType(), actual.getProviderType());
        Assertions.assertEquals(expected.getUserAccount().getId(), actual.getUserAccount().getId());
        Assertions.assertEquals(expected.getUserAccount().getEmail(), actual.getUserAccount().getEmail());
        Assertions.assertEquals(expected.getUserAccount().getUserId(), actual.getUserAccount().getUserId());
        Assertions.assertEquals((int) actual.getBiography().getId(), 1);
        Assertions.assertEquals(actual.getBiography().getFirstName(), "Тест");
        Assertions.assertEquals(actual.getBiography().getLastName(), "Тест");
    }

    private void createTables() {
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS \"user\" (\n" +
                        "  id SERIAL PRIMARY KEY,\n" +
                        "  created_at TIMESTAMP DEFAULT NOW(),\n" +
                        "  provider_id VARCHAR(30) NOT NULL,\n" +
                        "  deleted BOOLEAN NOT NULL DEFAULT FALSE\n" +
                        ")"
        );

        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS \"user_account\" (\n" +
                        "  id SERIAL PRIMARY KEY,\n" +
                        "  email VARCHAR(128) UNIQUE NOT NULL,\n" +
                        "  password VARCHAR(1024) NOT NULL,\n" +
                        "  user_id INTEGER NOT NULL REFERENCES \"user\"(id)\n" +
                        ")"
        );

        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS biography (\n" +
                        "  id                SERIAL PRIMARY KEY,\n" +
                        "  first_name        VARCHAR(512) NOT NULL,\n" +
                        "  last_name         VARCHAR(512) NOT NULL,\n" +
                        "  middle_name       VARCHAR(512),\n" +
                        "  creator_id        INTEGER      NOT NULL REFERENCES \"user\" (id),\n" +
                        "  user_id           INTEGER UNIQUE REFERENCES \"user\" (id),\n" +
                        "  biography         TEXT,\n" +
                        "  created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),\n" +
                        "  updated_at        TIMESTAMP    NOT NULL DEFAULT NOW(),\n" +
                        "  moderation_status INTEGER      NOT NULL DEFAULT 0,\n" +
                        "  moderation_info   TEXT,\n" +
                        "  moderated_at      TIMESTAMP,\n" +
                        "  moderator_id      INTEGER REFERENCES \"user\" (id),\n" +
                        "  publish_status    INTEGER      NOT NULL DEFAULT 0\n" +
                        ")"
        );
    }

    private void deleteTables() {
        jdbcTemplate.execute(
                "DROP TABLE IF EXISTS user_account"
        );

        jdbcTemplate.execute(
                "DROP TABLE IF EXISTS biography"
        );

        jdbcTemplate.execute(
                "DROP TABLE IF EXISTS \"user\""
        );
    }

    private void createUserBiography() {
        jdbcTemplate.update(
                "INSERT INTO biography(first_name, last_name, creator_id, user_id) VALUES('Тест', 'Тест', 1, 1)"
        );
    }
}