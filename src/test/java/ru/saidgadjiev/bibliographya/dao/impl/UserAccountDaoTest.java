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
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.FilterOperation;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.domain.UsersStats;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class UserDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserDao userAccountDao;

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
        //TODO: Test
    }

    @Test
    void getByUsername() {
        //TODO: Test
    }


    @Test
    void getUsers() {
        /*Assertions.assertTrue(userDao.getUsers(10, 0L, Collections.emptyList()).isEmpty());
        createUser(ProviderType.FACEBOOK);
        createUser(ProviderType.VK);
        createUserRole(1, "ROLE_ADMIN");
        createUserRole(2, "ROLE_TEST");

        Biography biography1 = createBiography(1, 1, "Test1", "Test1");

        createUserBiography(biography1);

        Biography biography2 = createBiography(2, 2, "Test2", "Test2");

        createUserBiography(biography2);

        List<User> userList = userDao.getUsers(10, 0L, Collections.emptyList());

        assertEquals(
                userList,
                new HashMap<Integer, User>() {{
                    put(1, createUser(1, ProviderType.FACEBOOK, "ROLE_ADMIN"));
                    put(2, createUser(2, ProviderType.VK, "ROLE_TEST"));
                }},
                new HashMap<Integer, Biography>() {{
                    put(1, biography1);
                    put(2, biography2);
                }}
        );

        List<FilterCriteria> criteria = new ArrayList<>();

        criteria.add(
                new FilterCriteria.Builder<String>()
                        .propertyName("role_name")
                        .filterOperation(FilterOperation.EQ)
                        .filterValue("ROLE_ADMIN")
                        .needPreparedSet(true)
                        .valueSetter(PreparedStatement::setString)
                        .build()
        );

        List<User> filteredList = userDao.getUsers(10, 0L, criteria);

        assertEquals(
                filteredList,
                new HashMap<Integer, User>() {{
                    put(1, createUser(1, ProviderType.FACEBOOK, "ROLE_ADMIN"));
                }},
                Collections.singletonMap(1, biography1)
        );*/
    }

    @Test
    void getStats() {
        /*createUser(ProviderType.FACEBOOK);
        createUser(ProviderType.VK);

        UsersStats usersStats = userDao.getStats();

        Assertions.assertEquals(usersStats.getCount(), 2);
        Assertions.assertEquals((int) usersStats.getUsersByProvider().get(ProviderType.FACEBOOK), 1);
        Assertions.assertEquals((int) usersStats.getUsersByProvider().get(ProviderType.VK), 1);*/
    }

    @Test
    void markDelete() {
        /*createUser(ProviderType.FACEBOOK);

        Biography biography1 = createBiography(1, 1, "Test1", "Test1");

        createUserBiography(biography1);

        userDao.markDelete(1, true);
        List<User> users = userDao.getUsers(10, 0L, Collections.emptyList());

        Assertions.assertTrue(users.get(0).isDeleted());*/
    }

    @Test
    void isExistUsername() {
        /*UserAccount userAccount = new UserAccount();

        userAccount.setEmail("test");
        userAccount.setPassword("test");

        User user = new User();

        user.setProviderType(ProviderType.EMAIL_PASSWORD);
        user.setUserAccount(userAccount);

        Assertions.assertFalse(userAccountDao.isExistEmail("test"));

        userAccountDao.save(user);

        Assertions.assertTrue(userAccountDao.isExistEmail("test"));*/
    }

    private void assertEquals(User expected, User actual) {
        /*Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.getProviderType(), actual.getProviderType());
        Assertions.assertEquals(expected.getUserAccount().getId(), actual.getUserAccount().getId());
        Assertions.assertEquals(expected.getUserAccount().getEmail(), actual.getUserAccount().getEmail());
        Assertions.assertEquals(expected.getUserAccount().getUserId(), actual.getUserAccount().getUserId());
        Assertions.assertEquals((int) actual.getBiography().getId(), 1);
        Assertions.assertEquals(actual.getBiography().getFirstName(), "Тест");
        Assertions.assertEquals(actual.getBiography().getLastName(), "Тест");*/
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