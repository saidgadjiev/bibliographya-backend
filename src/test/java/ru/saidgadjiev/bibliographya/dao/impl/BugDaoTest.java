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
import ru.saidgadjiev.bibliographya.domain.Bug;
import ru.saidgadjiev.bibliographya.utils.TableUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class BugDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BugDao bugDao;

    @BeforeEach
    void init() {
        TableUtils.createTableUser(jdbcTemplate);
        TableUtils.createTableBiography(jdbcTemplate);
        TableUtils.createBugTable(jdbcTemplate);
    }

    @AfterEach
    void after() {
        TableUtils.deleteTableBug(jdbcTemplate);
        TableUtils.deleteTableBiography(jdbcTemplate);
        TableUtils.deleteTableUser(jdbcTemplate);
    }

    @Test
    void create() {
        Bug bug = new Bug();

        bug.setTheme("Тест");
        bug.setBugCase("Тест");

        int created = bugDao.create(TimeZone.getDefault(), bug) != null ? 1 : 0;

        Bug actual = jdbcTemplate.query(
                "SELECT * FROM bug WHERE id = 1",
                resultSet -> {
                    if (resultSet.next()) {
                        return map(resultSet);
                    }

                    return null;
                }
        );

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(created, 1);
        Assertions.assertEquals(1, (int) actual.getId());
        Assertions.assertEquals("Тест", actual.getTheme());
        Assertions.assertEquals("Тест", actual.getBugCase());
        Assertions.assertEquals(Bug.BugStatus.PENDING, actual.getStatus());
        Assertions.assertNotNull(actual.getCreatedAt());
        Assertions.assertNull(actual.getFixedAt());
        Assertions.assertNull(actual.getFixerId());
        Assertions.assertNull(actual.getInfo());
    }

    @Test
    void update() {
        jdbcTemplate.update(
                "INSERT INTO bug(theme, bug_case) VALUES('ТемаС1', 'БагС1')"
        );
        jdbcTemplate.update(
                "INSERT INTO bug(theme, bug_case) VALUES('ТемаС2', 'БагС2')"
        );

        List<UpdateValue> values = new ArrayList<>();

        values.add(
                new UpdateValue<>(
                        "theme",
                        (preparedStatement, index) -> preparedStatement.setString(index, "ТемаО1")
                )
        );
        values.add(
                new UpdateValue<>(
                        "bug_case",
                        (preparedStatement, index) -> preparedStatement.setString(index, "БагО1")
                )
        );

        bugDao.update(values, new AndCondition() {{
            add(new Equals(new ColumnSpec("id"), new Param()));
        }}, new ArrayList<PreparedSetter>() {{
            add((preparedStatement, index) -> preparedStatement.setInt(index, 1));
        }});

        Bug actual = jdbcTemplate.query(
                "SELECT * FROM bug WHERE id = 1",
                resultSet -> {
                    if (resultSet.next()) {
                        return map(resultSet);
                    }

                    return null;
                }
        );

        Bug excepted = new Bug();

        excepted.setId(1);
        excepted.setTheme("ТемаО1");
        excepted.setBugCase("БагО1");
        excepted.setStatus(Bug.BugStatus.PENDING);

        Assertions.assertNotNull(actual);
        assertEquals(excepted, actual, Collections.emptySet());

        Bug actual1 = jdbcTemplate.query(
                "SELECT * FROM bug WHERE id = 2",
                resultSet -> {
                    if (resultSet.next()) {
                        return map(resultSet);
                    }

                    return null;
                }
        );

        Bug excepted1 = new Bug();

        excepted1.setId(2);
        excepted1.setTheme("ТемаС2");
        excepted1.setBugCase("БагС2");
        excepted1.setStatus(Bug.BugStatus.PENDING);

        Assertions.assertNotNull(actual1);
        assertEquals(excepted1, actual1, Collections.emptySet());
    }

    @Test
    void getFixerInfo() {
        createUser();
        createUserBiography();

        jdbcTemplate.update(
                "INSERT INTO bug(theme, bug_case, fixer_id) VALUES('Тест', 'Тест', 1)"
        );

        Bug bug = bugDao.getFixerInfo(1);

        Assertions.assertNotNull(bug);
        Assertions.assertEquals(Bug.BugStatus.PENDING, bug.getStatus());
        Assertions.assertEquals(1, (int) bug.getId());
        Assertions.assertEquals(1, (int) bug.getFixerId());
        Assertions.assertEquals(1, (int) bug.getFixer().getId());
        Assertions.assertEquals(1, (int) bug.getFixer().getUserId());
        Assertions.assertEquals("Test", bug.getFixer().getFirstName());
        Assertions.assertEquals("Test", bug.getFixer().getLastName());
    }

    private void assertEquals(Bug expected, Bug actual, Set<String> fields) {
        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.getTheme(), actual.getTheme());
        Assertions.assertEquals(expected.getBugCase(), actual.getBugCase());
        Assertions.assertEquals(expected.getStatus(), actual.getStatus());
        Assertions.assertNotNull(actual.getCreatedAt());
        Assertions.assertEquals(expected.getFixedAt(), actual.getFixedAt());
        Assertions.assertEquals(expected.getFixerId(), actual.getFixerId());
        Assertions.assertEquals(expected.getInfo(), actual.getInfo());

        if (fields.contains("fixer")) {
            Assertions.assertEquals(expected.getFixer().getId(), actual.getFixer().getId());
            Assertions.assertEquals(expected.getFixer().getFirstName(), actual.getFixer().getFirstName());
            Assertions.assertEquals(expected.getFixer().getLastName(), actual.getFixer().getLastName());
        }
    }

    private Bug map(ResultSet resultSet) throws SQLException {
        Bug result = new Bug();

        result.setId(resultSet.getInt("id"));
        result.setTheme(resultSet.getString("theme"));
        result.setBugCase(resultSet.getString("bug_case"));
        result.setStatus(Bug.BugStatus.fromCode(resultSet.getInt("status")));
        result.setCreatedAt(resultSet.getTimestamp("created_at"));

        return result;
    }

    private void createUser() {
        jdbcTemplate.update(
                "INSERT INTO \"user\"(email, password) VALUES(?, ?)",
                preparedStatement -> {
                    preparedStatement.setString(1, "Test");
                    preparedStatement.setString(2, "Test");
                }
        );
    }

    private void createUserBiography() {
        jdbcTemplate.update(
                "INSERT INTO biography(first_name, last_name, creator_id, user_id) VALUES('Test', 'Test', 1, 1)"
        );
    }
}