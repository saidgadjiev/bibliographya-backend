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
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.Bug;

import java.sql.PreparedStatement;
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
        createTables();
    }

    @AfterEach
    void after() {
        deleteTables();
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
                        "ТемаО1",
                        PreparedStatement::setString
                )
        );
        values.add(
                new UpdateValue<>(
                        "bug_case",
                        "БагО1",
                        PreparedStatement::setString
                )
        );
        Collection<FilterCriteria> criteria = new ArrayList<>();

        criteria.add(
                new FilterCriteria.Builder<Integer>()
                        .propertyName("id")
                        .filterValue(1)
                        .needPreparedSet(true)
                        .filterOperation(FilterOperation.EQ)
                        .valueSetter(PreparedStatement::setInt)
                        .build()
        );


        bugDao.update(values, criteria);

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
        createUser(ProviderType.FACEBOOK);
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
        Assertions.assertEquals("Тест", bug.getFixer().getFirstName());
        Assertions.assertEquals("Тест", bug.getFixer().getLastName());
    }

    @Test
    void getListWithFields() {
        createUser(ProviderType.FACEBOOK);
        createUserBiography();

        jdbcTemplate.update(
                "INSERT INTO bug(theme, bug_case, fixer_id) VALUES('Тест', 'Тест', 1)"
        );
        jdbcTemplate.update(
                "INSERT INTO bug(theme, bug_case, fixer_id) VALUES('Тест2', 'Тест2', 1)"
        );

        List<Bug> bugsWithFixerList = bugDao.getList(null, 10, 0L, null, Collections.emptyList(), Collections.singleton("fixer"));

        Assertions.assertEquals(2, bugsWithFixerList.size());
        Bug expected1 = new Bug();

        expected1.setId(1);
        expected1.setTheme("Тест");
        expected1.setBugCase("Тест");
        expected1.setFixerId(1);
        expected1.setStatus(Bug.BugStatus.PENDING);
        Biography fixer = new Biography();

        fixer.setId(1);
        fixer.setFirstName("Тест");
        fixer.setLastName("Тест");

        expected1.setFixer(fixer);

        assertEquals(expected1, bugsWithFixerList.get(0), Collections.singleton("fixer"));

        expected1.setId(2);
        expected1.setTheme("Тест2");
        expected1.setBugCase("Тест2");

        assertEquals(expected1, bugsWithFixerList.get(1), Collections.singleton("fixer"));
    }

    @Test
    void getListWithoutFields() {
        createUser(ProviderType.FACEBOOK);
        createUserBiography();

        jdbcTemplate.update(
                "INSERT INTO bug(theme, bug_case, fixer_id) VALUES('Тест', 'Тест', 1)"
        );
        jdbcTemplate.update(
                "INSERT INTO bug(theme, bug_case, fixer_id) VALUES('Тест2', 'Тест2', 1)"
        );

        List<Bug> bugsList = bugDao.getList(null, 10, 0L, null, Collections.emptyList(), Collections.emptySet());
        Assertions.assertEquals(2, bugsList.size());

        Bug expected1 = new Bug();

        expected1.setId(1);
        expected1.setTheme("Тест");
        expected1.setBugCase("Тест");
        expected1.setStatus(Bug.BugStatus.PENDING);
        expected1.setFixerId(1);

        assertEquals(expected1, bugsList.get(0), Collections.emptySet());

        expected1.setId(2);
        expected1.setTheme("Тест2");
        expected1.setBugCase("Тест2");

        assertEquals(expected1, bugsList.get(1), Collections.emptySet());
    }

    @Test
    void getListWithCriteria() {
        createUser(ProviderType.FACEBOOK);
        createUserBiography();

        jdbcTemplate.update(
                "INSERT INTO bug(theme, bug_case, fixer_id) VALUES('Тест', 'Тест', 1)"
        );
        jdbcTemplate.update(
                "INSERT INTO bug(theme, bug_case, fixer_id) VALUES('Тест2', 'Тест2', 1)"
        );

        List<FilterCriteria> criteria = new ArrayList<>();

        criteria.add(
                new FilterCriteria.Builder<String>()
                        .propertyName("bug_case")
                        .valueSetter(PreparedStatement::setString)
                        .filterValue("Тест2")
                        .needPreparedSet(true)
                        .filterOperation(FilterOperation.EQ)
                        .build()
        );

        List<Bug> bugsList = bugDao.getList(null, 10, 0L, null, criteria, Collections.emptySet());

        Assertions.assertEquals(1, bugsList.size());

        Bug expected1 = new Bug();

        expected1.setId(2);
        expected1.setTheme("Тест2");
        expected1.setBugCase("Тест2");
        expected1.setStatus(Bug.BugStatus.PENDING);
        expected1.setFixerId(1);

        assertEquals(expected1, bugsList.get(0), Collections.emptySet());
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

        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS bug (\n" +
                        "  id       SERIAL PRIMARY KEY,\n" +
                        "  created_at TIMESTAMP NOT NULL DEFAULT now(),\n" +
                        "  fixed_at TIMESTAMP,\n" +
                        "  theme    TEXT NOT NULL,\n" +
                        "  bug_case TEXT NOT NULL,\n" +
                        "  fixer_id INTEGER REFERENCES \"user\" (id),\n" +
                        "  status   INTEGER DEFAULT 0,\n" +
                        "  info     TEXT\n" +
                        ");;"
        );
    }

    private void deleteTables() {
        jdbcTemplate.execute(
                "DROP TABLE bug"
        );

        jdbcTemplate.execute(
                "DROP TABLE IF EXISTS biography"
        );

        jdbcTemplate.execute(
                "DROP TABLE \"user\""
        );
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

    private void createUserBiography() {
        jdbcTemplate.update(
                "INSERT INTO biography(first_name, last_name, creator_id, user_id) VALUES('Тест', 'Тест', 1, 1)"
        );
    }

    private void createUser(ProviderType providerType) {
        jdbcTemplate.update(
                "INSERT INTO \"user\"(provider_id) VALUES('" + providerType.getId() + "')"
        );
    }
}