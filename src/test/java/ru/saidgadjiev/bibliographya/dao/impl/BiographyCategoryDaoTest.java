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
import ru.saidgadjiev.bibliographya.domain.BiographyCategory;

import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class BiographyCategoryDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private BiographyCategoryDao biographyCategoryDao;

    @BeforeEach
    void init() {
        createTables();
    }

    @AfterEach
    void after() {
        deleteTables();
    }

    @Test
    void getList() {
        jdbcTemplate.update(
                "INSERT INTO biography_category(name, image_path) VALUES('test', 'test.jpg')"
        );
        jdbcTemplate.update(
                "INSERT INTO biography_category(name, image_path) VALUES('test1', 'test1.jpg')"
        );

        List<BiographyCategory> categories = biographyCategoryDao.getList(10, 0L);

        Assertions.assertEquals(categories.size(), 2);
        Assertions.assertEquals(categories.get(0).getName(), "test");
        Assertions.assertEquals(categories.get(0).getImagePath(), "test.jpg");
        Assertions.assertEquals(categories.get(1).getName(), "test1");
        Assertions.assertEquals(categories.get(1).getImagePath(), "test1.jpg");
    }

    @Test
    void getByName() {
        jdbcTemplate.update(
                "INSERT INTO biography_category(name, image_path) VALUES('test', 'test.jpg')"
        );

        BiographyCategory biographyCategory = biographyCategoryDao.getByName("test");

        Assertions.assertEquals(biographyCategory.getName(), "test");
        Assertions.assertEquals(biographyCategory.getImagePath(), "test.jpg");
    }

    @Test
    void countOff() {
        jdbcTemplate.update(
                "INSERT INTO biography_category(name, image_path) VALUES('test', 'test.jpg')"
        );

        Assertions.assertEquals(1, biographyCategoryDao.countOff());
    }

    @Test
    void create() {
        BiographyCategory category = new BiographyCategory();

        category.setImagePath("test.jpg");
        category.setName("test");

        BiographyCategory created = biographyCategoryDao.create(category);

        BiographyCategory biographyCategory = jdbcTemplate.query(
                "SELECT * FROM biography_category WHERE id = 1",
                resultSet -> {
                    if (resultSet.next()) {
                        BiographyCategory result = new BiographyCategory();

                        result.setName(resultSet.getString("name"));
                        result.setImagePath(resultSet.getString("image_path"));
                        result.setId(resultSet.getInt("id"));

                        return result;
                    }

                    return null;
                }
        );

        Assertions.assertNotNull(biographyCategory);
        Assertions.assertEquals(biographyCategory.getName(), "test");
        Assertions.assertEquals(biographyCategory.getImagePath(), "test.jpg");
        Assertions.assertEquals(created.getName(), "test");
        Assertions.assertEquals(created.getImagePath(), "test.jpg");
    }

    @Test
    void deleteByName() {
        jdbcTemplate.update(
                "INSERT INTO biography_category(name, image_path) VALUES('test', 'test.jpg')"
        );

        int deleted = biographyCategoryDao.deleteByName("test");

        Assertions.assertEquals(1, deleted);

        BiographyCategory biographyCategory = jdbcTemplate.query(
                "SELECT * FROM biography_category WHERE id = 1",
                resultSet -> {
                    if (resultSet.next()) {
                        BiographyCategory result = new BiographyCategory();

                        result.setName(resultSet.getString("name"));
                        result.setImagePath(resultSet.getString("image_path"));
                        result.setId(resultSet.getInt("id"));

                        return result;
                    }

                    return null;
                }
        );

        Assertions.assertNull(biographyCategory);
    }

    @Test
    void update() {
        jdbcTemplate.update(
                "INSERT INTO biography_category(name, image_path) VALUES('test', 'test.jpg')"
        );

        BiographyCategory biographyCategory = new BiographyCategory();

        biographyCategory.setId(1);
        biographyCategory.setName("test1");
        biographyCategory.setImagePath("test1.jpg");

        int deleted = biographyCategoryDao.update(biographyCategory);

        Assertions.assertEquals(1, deleted);

        BiographyCategory actual = jdbcTemplate.query(
                "SELECT * FROM biography_category WHERE id = 1",
                resultSet -> {
                    if (resultSet.next()) {
                        BiographyCategory result = new BiographyCategory();

                        result.setName(resultSet.getString("name"));
                        result.setImagePath(resultSet.getString("image_path"));
                        result.setId(resultSet.getInt("id"));

                        return result;
                    }

                    return null;
                }
        );

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(actual.getName(), "test1");
        Assertions.assertEquals(actual.getImagePath(), "test1.jpg");
    }

    private void createTables() {
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS biography_category (\n" +
                        "  id SERIAL PRIMARY KEY,\n" +
                        "  name VARCHAR(128) NOT NULL UNIQUE,\n" +
                        "  image_path VARCHAR(128) NOT NULL\n" +
                        ")"
        );
    }

    private void deleteTables() {
        jdbcTemplate.execute(
                "DROP TABLE IF EXISTS biography_category"
        );
    }
}