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
import ru.saidgadjiev.bibliographya.data.PreparedSetter;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.column.ColumnSpec;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.AndCondition;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.Equals;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.literals.Param;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.domain.UserAccount;
import ru.saidgadjiev.bibliographya.utils.TableUtils;
import ru.saidgadjiev.bibliographya.utils.TestModelsUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class GeneralDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private GeneralDao generalDao;

    @BeforeEach
    void before() {
        TableUtils.createTableUser(jdbcTemplate);
    }

    @AfterEach
    void after() {
        TableUtils.deleteTableUser(jdbcTemplate);
    }

    @Test
    void update() {
        /*jdbcTemplate.update(
                "INSERT INTO \"user\"(email, password, phone) VALUES(?, ?, ?)",
                preparedStatement -> {
                    preparedStatement.setString(1, "Test");
                    preparedStatement.setString(2, "Test");
                    preparedStatement.setString(3, "Test");
                }
        );
        jdbcTemplate.update(
                "INSERT INTO \"user\"(email, password, phone) VALUES(?, ?, ?)",
                preparedStatement -> {
                    preparedStatement.setString(1, "Test");
                    preparedStatement.setString(2, "Test");
                    preparedStatement.setString(3, "Test");
                }
        );

        List<UpdateValue> updateValues = new ArrayList<>();

        updateValues.add(
                new UpdateValue<>(
                        UserAccount.EMAIL,
                        ((preparedStatement, index) -> preparedStatement.setString(index, TestModelsUtils.TEST_EMAIL))
                )
        );

        int updated = generalDao.update(User.TABLE, updateValues, new AndCondition() {{
            add(new Equals(new ColumnSpec(User.ID), new Param()));
        }}, new ArrayList<PreparedSetter>() {{
            add((preparedStatement, index) -> preparedStatement.setInt(index, TestModelsUtils.TEST_USER_ID));
        }},null);

        Assertions.assertEquals(1, updated);

        User actual = jdbcTemplate.query(
                "SELECT u.* FROM \"user\" u WHERE u.id = " + TestModelsUtils.TEST_USER_ID,
                resultSet -> {
                    if (resultSet.next()) {
                        User user = new User();

                        user.setId(resultSet.getInt("id"));
                        user.setEmail(resultSet.getString("email"));
                        user.setPassword(resultSet.getString("password"));

                        return user;
                    }

                    return null;
                }
        );

        Assertions.assertEquals(TestModelsUtils.TEST_EMAIL, actual.getEmail());*/
    }

    @Test
    void getFields() {
        /*jdbcTemplate.update(
                "INSERT INTO \"user\"(email, password, phone) VALUES(?, ?, ?)",
                preparedStatement -> {
                    preparedStatement.setString(1, "Test1");
                    preparedStatement.setString(2, "Test");
                    preparedStatement.setString(3, "Test");
                }
        );
        jdbcTemplate.update(
                "INSERT INTO \"user\"(email, password, phone) VALUES(?, ?, ?)",
                preparedStatement -> {
                    preparedStatement.setString(1, "Test2");
                    preparedStatement.setString(2, "Test");
                    preparedStatement.setString(3, "Test");
                }
        );

        List<Map<String, Object>> values = generalDao.getFields(UserAccount.TABLE, Arrays.asList(UserAccount.EMAIL), new AndCondition() {{
            add(new Equals(new ColumnSpec(UserAccount.ID), new Param()));
        }}, new ArrayList<PreparedSetter>() {{
            add((preparedStatement, index) -> preparedStatement.setInt(index, TestModelsUtils.TEST_USER_ID));
        }});

        Assertions.assertEquals(1, values.size());
        Assertions.assertEquals(values.get(0).get(UserAccount.EMAIL), "Test1");*/
    }
}