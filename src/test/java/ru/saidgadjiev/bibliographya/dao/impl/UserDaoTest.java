package ru.saidgadjiev.bibliographya.dao.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.FilterOperation;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.Role;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.domain.UsersStats;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class UserDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserDao userDao;

    @BeforeEach
    void init() {
        createTables();
        createRole("ROLE_ADMIN");
        createRole("ROLE_TEST");
    }

    @AfterEach
    void after() {
        deleteTables();
    }

    @Test
    void getUsers() {
        Assertions.assertTrue(userDao.getUsers(10, 0L, Collections.emptyList()).isEmpty());
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
        );
    }

    @Test
    void getStats() {
        createUser(ProviderType.FACEBOOK);
        createUser(ProviderType.VK);

        UsersStats usersStats = userDao.getStats();

        Assertions.assertEquals(usersStats.getCount(), 2);
        Assertions.assertEquals((int) usersStats.getUsersByProvider().get(ProviderType.FACEBOOK), 1);
        Assertions.assertEquals((int) usersStats.getUsersByProvider().get(ProviderType.VK), 1);
    }

    @Test
    void markDelete() {
        createUser(ProviderType.FACEBOOK);

        Biography biography1 = createBiography(1, 1, "Test1", "Test1");

        createUserBiography(biography1);

        userDao.markDelete(1, true);
        List<User> users = userDao.getUsers(10, 0L, Collections.emptyList());

        Assertions.assertTrue(users.get(0).isDeleted());
    }

    private void assertEquals(List<User> userList,
                              Map<Integer, User> expectedList,
                              Map<Integer, Biography> biographies) {
        for (int i = 0; i < userList.size(); ++i) {
            User user = userList.get(i);
            Biography biography = biographies.get(user.getId());
            User expected = expectedList.get(user.getId());

            Assertions.assertEquals(user.getId(), expected.getId());
            Assertions.assertEquals(user.getProviderType(), expected.getProviderType());
            Assertions.assertIterableEquals(user.getRoles(), expected.getRoles());
            Assertions.assertEquals(user.getBiography().getFirstName(), biography.getFirstName());
            Assertions.assertEquals(user.getBiography().getLastName(), biography.getLastName());
            Assertions.assertEquals((int) user.getBiography().getId(), (int) biography.getId());
            Assertions.assertEquals((int) user.getBiography().getUserId(), (int) biography.getUserId());
            Assertions.assertFalse(user.isDeleted());
        }
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
                "CREATE TABLE IF NOT EXISTS role (\n" +
                        "  id SERIAL PRIMARY KEY,\n" +
                        "  name VARCHAR(255) NOT NULL UNIQUE\n" +
                        ")"
        );

        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS user_role (\n" +
                        "  id SERIAL PRIMARY KEY,\n" +
                        "  user_id INTEGER NOT NULL REFERENCES \"user\"(id),\n" +
                        "  role_name VARCHAR (255) NOT NULL REFERENCES role(name),\n" +
                        "  UNIQUE (user_id, role_name)\n" +
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
                "DROP TABLE IF EXISTS biography"
        );

        jdbcTemplate.execute(
                "DROP TABLE IF EXISTS user_role"
        );

        jdbcTemplate.execute(
                "DROP TABLE IF EXISTS role"
        );

        jdbcTemplate.execute(
                "DROP TABLE IF EXISTS \"user\""
        );
    }


    private void createRole(String role) {
        jdbcTemplate.update("INSERT INTO role(name) VALUES('" + role + "')");
    }

    private void createUser(ProviderType providerType) {
        jdbcTemplate.update(
                "INSERT INTO \"user\"(provider_id) VALUES('" + providerType.getId() + "')"
        );
    }

    private void createUserRole(int userId, String role) {
        jdbcTemplate.update(
                "INSERT INTO user_role(user_id, role_name) VALUES(" + userId + ",'" + role + "')"
        );
    }

    private void createUserBiography(Biography biography) {
        jdbcTemplate.update(
                "INSERT INTO biography(first_name, last_name, creator_id, user_id) VALUES(?, ?, ?, ?)",
                new PreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement preparedStatement) throws SQLException {
                        preparedStatement.setString(1, biography.getFirstName());
                        preparedStatement.setString(2, biography.getLastName());
                        preparedStatement.setInt(3, biography.getCreatorId());
                        preparedStatement.setInt(4, biography.getUserId());
                    }
                }
        );
    }

    private Biography createBiography(int id, int userId, String firstName, String lastName) {
        Biography biography = new Biography();

        biography.setId(id);
        biography.setFirstName(firstName);
        biography.setLastName(lastName);
        biography.setCreatorId(userId);
        biography.setUserId(userId);

        return biography;
    }

    private User createUser(int id, ProviderType providerType, String ... roles) {
        User user = new User();

        user.setId(id);
        user.setProviderType(providerType);
        user.setRoles(Stream.of(roles).map(Role::new).collect(Collectors.toSet()));

        return user;
    }
}