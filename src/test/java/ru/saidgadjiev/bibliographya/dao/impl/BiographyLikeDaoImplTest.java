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
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.BiographyLike;
import ru.saidgadjiev.bibliographya.utils.TestAssertionsUtils;
import ru.saidgadjiev.bibliographya.utils.TestModelsUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class BiographyLikeDaoImplTest {

    @Autowired
    private BiographyLikeDaoImpl biographyLikeDao;

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
        BiographyLike like = new BiographyLike(1, 1);

        biographyLikeDao.create(like);

        List<BiographyLike> likes = jdbcTemplate.query(
                "SELECT * FROM biography_like",
                (resultSet, i) -> {
                    BiographyLike item = new BiographyLike();

                    item.setUserId(resultSet.getInt("user_id"));
                    item.setBiographyId(resultSet.getInt("biography_id"));

                    return item;
                }
        );

        Assertions.assertEquals(1, likes.size());

        TestAssertionsUtils.assertLikeEquals(likes.get(0), like);
    }

    @Test
    void delete() {
        BiographyLike like = new BiographyLike(1, 1);

        jdbcTemplate.update(
                "INSERT INTO biography_like(user_id, biography_id) VALUES(1, 1)"
        );

       int deleted = biographyLikeDao.delete(like);

       Assertions.assertEquals(deleted, 1);

       long result = jdbcTemplate.query(
               "SELECT COUNT(*) FROM biography_like",
               resultSet -> {
                   if (resultSet.next()) {
                       return resultSet.getLong(1);
                   }

                   return null;
               }
       );

       Assertions.assertEquals(result, 0L);
    }

    @Test
    void getLikesCount() {
        Assertions.assertEquals(0, biographyLikeDao.getLikesCount(1));

        jdbcTemplate.update(
                "INSERT INTO biography_like(user_id, biography_id) VALUES(1, 1)"
        );

        Assertions.assertEquals(1, biographyLikeDao.getLikesCount(1));
    }

    @Test
    void isLiked() {
        Assertions.assertFalse(biographyLikeDao.isLiked(1, 1));

        jdbcTemplate.update(
                "INSERT INTO biography_like(user_id, biography_id) VALUES(1, 1)"
        );

        Assertions.assertTrue(biographyLikeDao.isLiked(1, 1));
    }

    @Test
    void isLikedByBiographies() {
        Map<Integer, Boolean> values = biographyLikeDao.isLikedByBiographies(1, Collections.singleton(1));

        Assertions.assertFalse(values.get(1));

        jdbcTemplate.update(
                "INSERT INTO biography_like(user_id, biography_id) VALUES(1, 1)"
        );

        values = biographyLikeDao.isLikedByBiographies(1, Collections.singleton(1));

        Assertions.assertTrue(values.get(1));
    }

    @Test
    void getLikesCountByBiographies() {
        Map<Integer, Integer> values = biographyLikeDao.getLikesCountByBiographies(Collections.singleton(1));

        Assertions.assertEquals(0, (int) values.get(1));

        jdbcTemplate.update(
                "INSERT INTO biography_like(user_id, biography_id) VALUES(1, 1)"
        );

        values = biographyLikeDao.getLikesCountByBiographies(Collections.singleton(1));

        Assertions.assertEquals(1, (int) values.get(1));
    }

    @Test
    void countOff() {
        Assertions.assertEquals(0, biographyLikeDao.countOff());

        jdbcTemplate.update(
                "INSERT INTO biography_like(user_id, biography_id) VALUES(1, 1)"
        );

        Assertions.assertEquals(1, biographyLikeDao.countOff());
    }

    @Test
    void getLikes() {
        jdbcTemplate.update(
                "INSERT INTO biography_like(user_id, biography_id) VALUES(1, 1)"
        );

        List<BiographyLike> likes = biographyLikeDao.getLikes(1, 10, 0);

        Assertions.assertEquals(1, likes.size());

        BiographyLike like = new BiographyLike();
        Biography biography = new Biography();

        biography.setId(1);
        biography.setFirstName(TestModelsUtils.TEST_FIRST_NAME);
        biography.setLastName(TestModelsUtils.TEST_LAST_NAME);

        like.setUser(biography);

        TestAssertionsUtils.assertLikeEquals(likes.get(0), like);
    }

    private void createTestUser() {
        jdbcTemplate.update(
                "INSERT INTO \"user\"(provider_id) VALUES ('facebook')"
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
                "CREATE TABLE IF NOT EXISTS biography_like (\n" +
                        "  id SERIAL PRIMARY KEY,\n" +
                        "  biography_id INTEGER NOT NULL REFERENCES biography(id) ON DELETE CASCADE,\n" +
                        "  user_id INTEGER NOT NULL REFERENCES \"user\"(id),\n" +
                        "  UNIQUE (biography_id, user_id)\n" +
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