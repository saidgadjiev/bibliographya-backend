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
import ru.saidgadjiev.bibliographya.domain.BiographyComment;
import ru.saidgadjiev.bibliographya.utils.TestAssertionsUtils;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class BiographyCommentDaoImplTest {

    @Autowired
    private BiographyCommentDao biographyCommentDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void init() {
        createTables();
        createTestUser();
        createUserBiography();
    }

    @AfterEach
    void after() {
        deleteTables();
    }

    @Test
    void create() {
        BiographyComment comment = new BiographyComment();

        comment.setContent("Test");
        comment.setBiographyId(1);
        comment.setUserId(1);

        biographyCommentDao.create(comment);

        Assertions.assertEquals(1, comment.getId());
        Assertions.assertNotNull(comment.getCreatedAt());

        BiographyComment actual = jdbcTemplate.query(
                "SELECT * FROM biography_comment WHERE id = 1",
                resultSet -> {
                    if (resultSet.next()) {
                        BiographyComment result = new BiographyComment();

                        result.setId(resultSet.getInt("id"));
                        result.setBiographyId(resultSet.getInt("biography_id"));
                        result.setUserId(resultSet.getInt("user_id"));
                        result.setContent(resultSet.getString("content"));
                        result.setCreatedAt(resultSet.getTimestamp("created_at"));

                        return result;
                    }

                    return null;
                }
        );

        Assertions.assertNotNull(actual);
        TestAssertionsUtils.assertCommentsEquals(comment, actual);
    }

    @Test
    void delete() {
        jdbcTemplate.update(
                "INSERT INTO biography_comment(content, biography_id, user_id) VALUES ('Test', 1, 1)"
        );
        int deleted = biographyCommentDao.delete(1);

        Assertions.assertEquals(1, deleted);
        BiographyComment actual = jdbcTemplate.query(
                "SELECT * FROM biography_comment WHERE id = 1",
                resultSet -> {
                    if (resultSet.next()) {
                        return new BiographyComment();
                    }

                    return null;
                }
        );

        Assertions.assertNull(actual);
    }

    @Test
    void countOffByBiographyId() {
        jdbcTemplate.update(
                "INSERT INTO biography_comment(content, biography_id, user_id) VALUES ('Test', 2, 1)"
        );

        Assertions.assertEquals(1, biographyCommentDao.countOffByBiographyId(1));
        Assertions.assertEquals(0, biographyCommentDao.countOffByBiographyId(2));
    }

    @Test
    void countOff() {
    }

    @Test
    void countOffByBiographiesIds() {
    }

    @Test
    void getById() {
    }

    @Test
    void getFields() {
    }

    @Test
    void updateContent() {
    }


    private void createTestUser() {
        jdbcTemplate.update(
                "INSERT INTO \"user\"(password, email) VALUES ('test', 'test@mail.ru')"
        );
    }

    private void createUserBiography() {
        jdbcTemplate.update(
                "INSERT INTO biography(first_name, last_name, creator_id, user_id) VALUES('Test', 'Test', 1, 1)"
        );
    }

    private void createTables() {
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS \"user\" (\n" +
                        "  id SERIAL PRIMARY KEY,\n" +
                        "  created_at TIMESTAMP DEFAULT NOW(),\n" +
                        "  provider_id VARCHAR(30) NOT NULL,\n" +
                        "  deleted BOOLEAN NOT NULL DEFAULT FALSE\n" +
                        ");"
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
                        ");"
        );

        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS biography_comment (\n" +
                        "  id SERIAL PRIMARY KEY,\n" +
                        "  content TEXT NOT NULL,\n" +
                        "  created_at TIMESTAMP NOT NULL DEFAULT NOW(),\n" +
                        "  biography_id INTEGER NOT NULL REFERENCES biography(id) ON DELETE CASCADE,\n" +
                        "  user_id INTEGER NOT NULL REFERENCES \"user\"(id),\n" +
                        "  parent_id INTEGER REFERENCES biography_comment(id)\n" +
                        ")"
        );
    }

    private void deleteTables() {
        jdbcTemplate.execute(
                "DROP TABLE IF EXISTS biography_like"
        );

        jdbcTemplate.execute("DROP TABLE IF EXISTS \"user\"");

        jdbcTemplate.execute("DROP TABLE IF EXISTS biography");
    }
}