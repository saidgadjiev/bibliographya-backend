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
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.FilterOperation;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.Role;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.domain.UsersStats;
import ru.saidgadjiev.bibliographya.utils.TableUtils;
import ru.saidgadjiev.bibliographya.utils.TestAssertionsUtils;
import ru.saidgadjiev.bibliographya.utils.TestModelsUtils;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class UserDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserDao userDao;

    @BeforeEach
    void init() {
        TableUtils.createTableUser(jdbcTemplate);
        TableUtils.createTableBiography(jdbcTemplate);
        TableUtils.createRoleTable(jdbcTemplate);
        TableUtils.createUserRoleTable(jdbcTemplate);
    }

    @AfterEach
    void after() {
        TableUtils.deleteTableBiography(jdbcTemplate);
        TableUtils.deleteTableRole(jdbcTemplate);
        TableUtils.deleteTableUserRole(jdbcTemplate);
        TableUtils.deleteTableUser(jdbcTemplate);
    }

    @Test
    void save() {
        User saveUser = new User();

        saveUser.setEmail(TestModelsUtils.TEST_EMAIL);
        saveUser.setPassword("Test");

        userDao.save(saveUser);

        jdbcTemplate.update(
                "INSERT INTO biography(first_name, last_name, creator_id, user_id) VALUES(?, ?, ?, ?)",
                preparedStatement -> {
                    preparedStatement.setString(1, TestModelsUtils.TEST_FIRST_NAME);
                    preparedStatement.setString(2, TestModelsUtils.TEST_LAST_NAME);
                    preparedStatement.setInt(3, 1);
                    preparedStatement.setInt(4, 1);
                }
        );

        User actual = jdbcTemplate.query(
                "SELECT u.*, b.id as b_id, b.first_name, b.last_name, b.middle_name " +
                        "FROM \"user\" u LEFT JOIN biography b ON u.id = b.user_id WHERE u.id = " + TestModelsUtils.TEST_USER_ID,
                resultSet -> {
                    if (resultSet.next()) {
                        User user = new User();

                        user.setId(resultSet.getInt("id"));
                        user.setEmail(resultSet.getString("email"));
                        user.setPassword(resultSet.getString("password"));

                        user.setBiography(new Biography());
                        user.getBiography().setId(resultSet.getInt("b_id"));
                        user.getBiography().setFirstName(resultSet.getString("first_name"));
                        user.getBiography().setLastName(resultSet.getString("last_name"));

                        return user;
                    }

                    return null;
                }
        );

        saveUser.setBiography(new Biography());
        saveUser.getBiography().setId(1);
        saveUser.getBiography().setFirstName(TestModelsUtils.TEST_FIRST_NAME);
        saveUser.getBiography().setLastName(TestModelsUtils.TEST_LAST_NAME);

        TestAssertionsUtils.assertUserEquals(saveUser, actual);
    }

    @Test
    void get() {
        jdbcTemplate.update(
                "INSERT INTO \"user\"(email, password, email_verified) VALUES(?, ?, true)",
                preparedStatement -> {
                    preparedStatement.setString(1, TestModelsUtils.TEST_EMAIL);
                    preparedStatement.setString(2, "Test");
                }
        );
        jdbcTemplate.update(
                "INSERT INTO \"user\"(email, password, email_verified) VALUES(?, ?, false)",
                preparedStatement -> {
                    preparedStatement.setString(1, TestModelsUtils.TEST_EMAIL + "2");
                    preparedStatement.setString(2, "Test2");
                }
        );

        jdbcTemplate.update(
                "INSERT INTO biography(first_name, last_name, creator_id, user_id) VALUES(?, ?, ?, ?)",
                preparedStatement -> {
                    preparedStatement.setString(1, TestModelsUtils.TEST_FIRST_NAME);
                    preparedStatement.setString(2, TestModelsUtils.TEST_LAST_NAME);
                    preparedStatement.setInt(3, TestModelsUtils.TEST_USER_ID);
                    preparedStatement.setInt(4, TestModelsUtils.TEST_USER_ID);
                }
        );

        jdbcTemplate.update(
                "INSERT INTO biography(first_name, last_name, creator_id, user_id) VALUES(?, ?, ?, ?)",
                preparedStatement -> {
                    preparedStatement.setString(1, TestModelsUtils.TEST_FIRST_NAME + "2");
                    preparedStatement.setString(2, TestModelsUtils.TEST_LAST_NAME + "2");
                    preparedStatement.setInt(3, 2);
                    preparedStatement.setInt(4, 2);
                }
        );

        Collection<FilterCriteria> criteria = new ArrayList<>();

        criteria.add(
                new FilterCriteria.Builder<String>()
                        .valueSetter(PreparedStatement::setString)
                        .needPreparedSet(true)
                        .propertyName(User.EMAIL)
                        .filterValue(TestModelsUtils.TEST_EMAIL)
                        .filterOperation(FilterOperation.EQ)
                        .build()
        );

        User user = userDao.getUniqueUser(criteria);

        User expected = new User();

        expected.setId(TestModelsUtils.TEST_USER_ID);
        expected.setEmail(TestModelsUtils.TEST_EMAIL);
        expected.setPassword("Test");

        expected.setBiography(new Biography());
        expected.getBiography().setId(1);
        expected.getBiography().setFirstName(TestModelsUtils.TEST_FIRST_NAME);
        expected.getBiography().setLastName(TestModelsUtils.TEST_LAST_NAME);

        TestAssertionsUtils.assertUserEquals(expected, user);
    }


    @Test
    void getUsers() {
        Assertions.assertTrue(userDao.getUsers(10, 0L, Collections.emptyList()).isEmpty());

        jdbcTemplate.update(
                "INSERT INTO \"user\"(email, password, email_verified) VALUES(?, ?, true)",
                preparedStatement -> {
                    preparedStatement.setString(1, TestModelsUtils.TEST_EMAIL);
                    preparedStatement.setString(2, "Test");
                }
        );
        jdbcTemplate.update(
                "INSERT INTO \"user\"(email, password, email_verified) VALUES(?, ?, false)",
                preparedStatement -> {
                    preparedStatement.setString(1, TestModelsUtils.TEST_EMAIL + "2");
                    preparedStatement.setString(2, "Test2");
                }
        );
        jdbcTemplate.update(
                "INSERT INTO biography(first_name, last_name, creator_id, user_id) VALUES(?, ?, ?, ?)",
                preparedStatement -> {
                    preparedStatement.setString(1, TestModelsUtils.TEST_FIRST_NAME);
                    preparedStatement.setString(2, TestModelsUtils.TEST_LAST_NAME);
                    preparedStatement.setInt(3, TestModelsUtils.TEST_USER_ID);
                    preparedStatement.setInt(4, TestModelsUtils.TEST_USER_ID);
                }
        );
        jdbcTemplate.update(
                "INSERT INTO biography(first_name, last_name, creator_id, user_id) VALUES(?, ?, ?, ?)",
                preparedStatement -> {
                    preparedStatement.setString(1, TestModelsUtils.TEST_FIRST_NAME + "2");
                    preparedStatement.setString(2, TestModelsUtils.TEST_LAST_NAME + "2");
                    preparedStatement.setInt(3, 2);
                    preparedStatement.setInt(4, 2);
                }
        );

        jdbcTemplate.update(
                "INSERT INTO role(name) VALUES('ROLE_USER')"
        );
        jdbcTemplate.update(
                "INSERT INTO role(name) VALUES('ROLE_ADMIN')"
        );
        jdbcTemplate.update(
                "INSERT INTO user_role(user_id, role_name) VALUES(1, 'ROLE_USER')"
        );
        jdbcTemplate.update(
                "INSERT INTO user_role(user_id, role_name) VALUES(2, 'ROLE_ADMIN')"
        );

        Collection<FilterCriteria> criteria = new ArrayList<>();

        criteria.add(
                new FilterCriteria.Builder<String>()
                        .valueSetter(PreparedStatement::setString)
                        .filterOperation(FilterOperation.EQ)
                        .needPreparedSet(true)
                        .propertyName("role_name")
                        .filterValue(Role.ROLE_USER)
                        .build()
        );

        List<User> users = userDao.getUsers(10, 0L, criteria);

        Assertions.assertEquals(1, users.size());
        User expected = new User();

        expected.setId(TestModelsUtils.TEST_USER_ID);
        expected.setEmail(TestModelsUtils.TEST_EMAIL);
        expected.setPassword("Test");

        expected.setBiography(new Biography());
        expected.getBiography().setId(1);
        expected.getBiography().setFirstName(TestModelsUtils.TEST_FIRST_NAME);
        expected.getBiography().setLastName(TestModelsUtils.TEST_LAST_NAME);

        TestAssertionsUtils.assertUserEquals(expected, users.get(0));
    }

    @Test
    void getStats() {
        jdbcTemplate.update(
                "INSERT INTO \"user\"(email, password, email_verified) VALUES(?, ?, true)",
                preparedStatement -> {
                    preparedStatement.setString(1, TestModelsUtils.TEST_EMAIL);
                    preparedStatement.setString(2, "Test");
                }
        );
        jdbcTemplate.update(
                "INSERT INTO \"user\"(email, password, email_verified) VALUES(?, ?, false)",
                preparedStatement -> {
                    preparedStatement.setString(1, TestModelsUtils.TEST_EMAIL + "2");
                    preparedStatement.setString(2, "Test2");
                }
        );

        UsersStats usersStats = userDao.getStats();

        Assertions.assertEquals(usersStats.getCount(), 2);
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
    void isExistEmail() {
        jdbcTemplate.update(
                "INSERT INTO \"user\"(email, password, email_verified) VALUES(?, ?, true)",
                preparedStatement -> {
                    preparedStatement.setString(1, TestModelsUtils.TEST_EMAIL);
                    preparedStatement.setString(2, "Test");
                }
        );

        Assertions.assertFalse(userDao.isExistEmail("test"));
        Assertions.assertTrue(userDao.isExistEmail(TestModelsUtils.TEST_EMAIL));
    }
}