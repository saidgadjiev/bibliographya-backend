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
import ru.saidgadjiev.bibliographya.utils.TableUtils;
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
        TableUtils.createTableUser(jdbcTemplate);
        TableUtils.createTableBiography(jdbcTemplate);
        TableUtils.createTableBiographyComment(jdbcTemplate);
        createTestUser();
        createUserBiography();
    }

    @AfterEach
    void after() {
        TableUtils.deleteTableBiographyComment(jdbcTemplate);
        TableUtils.deleteTableBiography(jdbcTemplate);
        TableUtils.deleteTableUser(jdbcTemplate);
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
    void createReply() {
        jdbcTemplate.update(
                "INSERT INTO biography_comment(content, biography_id, user_id) VALUES('Test', 1, 1)"
        );
        BiographyComment comment = new BiographyComment();

        comment.setContent("TestReply");
        comment.setBiographyId(1);
        comment.setUserId(1);
        comment.setParentId(1);

        biographyCommentDao.create(comment);

        Assertions.assertEquals(2, comment.getId());
        Assertions.assertNotNull(comment.getCreatedAt());

        BiographyComment actual = jdbcTemplate.query(
                "SELECT * FROM biography_comment WHERE id = 2",
                resultSet -> {
                    if (resultSet.next()) {
                        BiographyComment result = new BiographyComment();

                        result.setId(resultSet.getInt("id"));
                        result.setBiographyId(resultSet.getInt("biography_id"));
                        result.setUserId(resultSet.getInt("user_id"));
                        result.setContent(resultSet.getString("content"));
                        result.setCreatedAt(resultSet.getTimestamp("created_at"));
                        result.setParentId(resultSet.getInt("parent_id"));
                        result.setParentUserId(resultSet.getInt("parent_user_id"));

                        return result;
                    }

                    return null;
                }
        );
        comment.setParentUserId(1);

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
                "INSERT INTO biography(first_name, last_name, creator_id) VALUES('Test', 'Test', 1)"
        );
        jdbcTemplate.update(
                "INSERT INTO biography_comment(content, biography_id, user_id) VALUES ('Test', 2, 1)"
        );

        Assertions.assertEquals(1, biographyCommentDao.countOffByBiographyId(2));
        Assertions.assertEquals(0, biographyCommentDao.countOffByBiographyId(1));
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