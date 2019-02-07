package ru.saidgadjiev.bibliographya.bussiness.bug;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.dao.impl.BugDao;
import ru.saidgadjiev.bibliographya.domain.Bug;
import ru.saidgadjiev.bibliographya.utils.ResultSetUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class BaseBugBusinessTest {

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected BugDao bugDao;

    @BeforeEach
    void init() {
        createTables();
    }

    @AfterEach
    void after() {
        deleteTables();
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

    protected void createUser() {
        createUser(ProviderType.FACEBOOK);
        createUserBiography(1);
    }

    protected void createUserBiography(int userId) {
        jdbcTemplate.update(
                "INSERT INTO biography(first_name, last_name, creator_id, user_id) VALUES('Тест', 'Тест', ?, ?)",
                preparedStatement -> {
                    preparedStatement.setInt(1, userId);
                    preparedStatement.setInt(2, userId);
                }
        );
    }

    protected void createUser(ProviderType providerType) {
        jdbcTemplate.update(
                "INSERT INTO \"user\"(provider_id) VALUES('" + providerType.getId() + "')"
        );
    }

    protected Bug map(ResultSet resultSet) throws SQLException {
        Bug result = new Bug();

        result.setId(resultSet.getInt("id"));
        result.setStatus(Bug.BugStatus.fromCode(resultSet.getInt("status")));
        result.setFixerId(ResultSetUtils.intOrNull(resultSet, "fixer_id"));
        result.setInfo(resultSet.getString("info"));

        return result;
    }
}
