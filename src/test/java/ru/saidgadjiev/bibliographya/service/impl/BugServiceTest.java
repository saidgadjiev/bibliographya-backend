package ru.saidgadjiev.bibliographya.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.saidgadjiev.bibliographya.bussiness.bug.Handler;
import ru.saidgadjiev.bibliographya.domain.Bug;
import ru.saidgadjiev.bibliographya.domain.CompleteResult;
import ru.saidgadjiev.bibliographya.model.BugRequest;
import ru.saidgadjiev.bibliographya.model.CompleteRequest;
import ru.saidgadjiev.bibliographya.utils.ResultSetUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class BugServiceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BugService bugService;

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
        BugRequest request = new BugRequest();

        request.setTheme("Тест");
        request.setBugCase("Тест");

        Bug created = bugService.create(request);

        Bug bug = jdbcTemplate.query(
                "SELECT * FROM bug WHERE id = 1",
                resultSet -> {
                    if (resultSet.next()) {
                        return map(resultSet);
                    }

                    return null;
                }
        );
        Assertions.assertNotNull(bug);
        Bug expected = new Bug();

        expected.setId(1);
        expected.setTheme("Тест");
        expected.setBugCase("Тест");
        expected.setStatus(Bug.BugStatus.PENDING);

        assertEquals(expected, bug);
        assertEquals(created, bug);
    }

    @Test
    void completePending() throws SQLException {
        jdbcTemplate.update(
                "INSERT INTO bug(theme, bug_case) VALUES('Тест', 'Тест')"
        );

        CompleteRequest completeRequest = new CompleteRequest();

        completeRequest.setSignal(Handler.Signal.ASSIGN_ME.getDesc());
        completeRequest.setStatus(Bug.BugStatus.PENDING.getCode());

        CompleteResult<bugService.complete(1, completeRequest);
    }

    @Test
    void getActions() {
    }

    @Test
    void getFixerInfo() {
    }

    protected void assertEquals(Bug expected, Bug actual) {
        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.getStatus(), actual.getStatus());
        Assertions.assertEquals(expected.getFixerId(), actual.getFixerId());
        Assertions.assertEquals(expected.getInfo(), actual.getInfo());
    }

    private Bug map(ResultSet resultSet) throws SQLException {
        Bug result = new Bug();

        result.setId(resultSet.getInt("id"));
        result.setStatus(Bug.BugStatus.fromCode(resultSet.getInt("status")));
        result.setCreatedAt(resultSet.getTimestamp("created_at"));
        result.setBugCase(resultSet.getString("bug_case"));
        result.setTheme(resultSet.getString("theme"));

        return result;
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
}