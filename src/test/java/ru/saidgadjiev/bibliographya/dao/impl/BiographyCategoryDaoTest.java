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
import ru.saidgadjiev.bibliographya.utils.TableUtils;

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
        TableUtils.createTableBiographyCategory(jdbcTemplate);
    }

    @AfterEach
    void after() {
        TableUtils.deleteTableBiographyCategory(jdbcTemplate);
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

        BiographyCategory biographyCategory = biographyCategoryDao.getById(1);

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
       /* BiographyCategory category = new BiographyCategory();

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
        Assertions.assertEquals(created.getImagePath(), "test.jpg");*/
    }

    @Test
    void deleteById() {
//        /*jdbcTemplate.update(
//                "INSERT INTO biography_category(name, image_path) VALUES('test', 'test.jpg')"
//        );
//
//        int deleted = biographyCategoryDao.deleteById(1);
//
//        Assertions.assertEquals(1, deleted);
//
//        BiographyCategory biographyCategory = jdbcTemplate.query(
//                "SELECT * FROM biography_category WHERE id = 1",
//                resultSet -> {
//                    if (resultSet.next()) {
//                        BiographyCategory result = new BiographyCategory();
//
//                        result.setName(resultSet.getString("name"));
//                        result.setImagePath(resultSet.getString("image_path"));
//                        result.setId(resultSet.getInt("id"));
//
//                        return result;
//                    }
//
//                    return null;
//                }
//        );
//
//        Assertions.assertNull(biographyCategory);*/
    }

    @Test
    void update() {
        /*jdbcTemplate.update(
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
        Assertions.assertEquals(actual.getImagePath(), "test1.jpg");*/
    }
}