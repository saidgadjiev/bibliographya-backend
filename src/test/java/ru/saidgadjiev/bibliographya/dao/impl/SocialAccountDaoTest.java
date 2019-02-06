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
import ru.saidgadjiev.bibliographya.domain.Role;
import ru.saidgadjiev.bibliographya.domain.SocialAccount;
import ru.saidgadjiev.bibliographya.domain.User;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class SocialAccountDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SocialAccountDao socialAccountDao;

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
        SocialAccount socialAccount = new SocialAccount();

        socialAccount.setAccountId("test");

        User user = new User();

        user.setProviderType(ProviderType.FACEBOOK);
        user.setRoles(Stream.of(new Role(Role.ROLE_SOCIAL_USER)).collect(Collectors.toSet()));
        user.setSocialAccount(socialAccount);

        User result = socialAccountDao.save(user);

        Assertions.assertEquals(result.getSocialAccount().getId(), 1);
        Assertions.assertEquals(result.getId(), 1);

        Assertions.assertNull(socialAccountDao.getByUserId(user.getId()));

        createUserBiography();

        assertEquals(result, socialAccountDao.getByUserId(user.getId()));
    }

    @Test
    void getByAccountId() {
        SocialAccount socialAccount = new SocialAccount();

        socialAccount.setAccountId("test");

        User user = new User();

        user.setProviderType(ProviderType.FACEBOOK);
        user.setRoles(Stream.of(new Role(Role.ROLE_SOCIAL_USER)).collect(Collectors.toSet()));
        user.setSocialAccount(socialAccount);

        User result = socialAccountDao.save(user);

        Assertions.assertEquals(result.getSocialAccount().getId(), 1);
        Assertions.assertEquals(result.getId(), 1);

        Assertions.assertNull(socialAccountDao.getByAccountId(ProviderType.FACEBOOK, socialAccount.getAccountId()));

        createUserBiography();

        assertEquals(result, socialAccountDao.getByAccountId(ProviderType.FACEBOOK, socialAccount.getAccountId()));
    }

    private void assertEquals(User expected, User actual) {
        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.getProviderType(), actual.getProviderType());
        Assertions.assertEquals(expected.getSocialAccount().getId(), actual.getSocialAccount().getId());
        Assertions.assertEquals(expected.getSocialAccount().getAccountId(), actual.getSocialAccount().getAccountId());
        Assertions.assertEquals(expected.getSocialAccount().getUserId(), actual.getSocialAccount().getUserId());
        Assertions.assertEquals((int) actual.getBiography().getId(), 1);
        Assertions.assertEquals(actual.getBiography().getFirstName(), "Тест");
        Assertions.assertEquals(actual.getBiography().getLastName(), "Тест");
    }

    private void createTables() {
        jdbcTemplate.execute(
                "CREATE TABLE \"user\" (\n" +
                        "  id SERIAL PRIMARY KEY,\n" +
                        "  created_at TIMESTAMP DEFAULT NOW(),\n" +
                        "  provider_id VARCHAR(30) NOT NULL,\n" +
                        "  deleted BOOLEAN NOT NULL DEFAULT FALSE\n" +
                        ")"
        );

        jdbcTemplate.execute(
                "CREATE TABLE \"social_account\" (\n" +
                        "  id SERIAL PRIMARY KEY,\n" +
                        "  account_id VARCHAR(30) UNIQUE NOT NULL,\n" +
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
                "DROP TABLE social_account"
        );

        jdbcTemplate.execute(
                "DROP TABLE \"user\""
        );

        jdbcTemplate.execute(
                "DROP TABLE biography"
        );
    }

    private void createUserBiography() {
        jdbcTemplate.update(
                "INSERT INTO biography(first_name, last_name, creator_id, user_id) VALUES('Тест', 'Тест', 1, 1)"
        );
    }
}