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
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.FilterOperation;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.utils.TableUtils;

import java.sql.PreparedStatement;
import java.util.*;

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
    void getFields() {
        jdbcTemplate.update(
                "INSERT INTO \"user\"(email, password, email_verified) VALUES(?, ?, true)",
                preparedStatement -> {
                    preparedStatement.setString(1, "Test1");
                    preparedStatement.setString(2, "Test");
                }
        );
        jdbcTemplate.update(
                "INSERT INTO \"user\"(email, password, email_verified) VALUES(?, ?, false)",
                preparedStatement -> {
                    preparedStatement.setString(1, "Test2");
                    preparedStatement.setString(2, "Test");
                }
        );

        Collection<FilterCriteria> criteria = new ArrayList<>();

        criteria.add(
                new FilterCriteria.Builder<Integer>()
                        .propertyName(User.ID)
                        .valueSetter(PreparedStatement::setInt)
                        .needPreparedSet(true)
                        .filterOperation(FilterOperation.EQ)
                        .filterValue(1)
                        .build()
        );

        List<Map<String, Object>> values = generalDao.getFields(User.TABLE, Arrays.asList(User.EMAIL, User.EMAIL_VERIFIED), criteria);

        Assertions.assertEquals(1, values.size());
        Assertions.assertEquals(values.get(0).get(User.EMAIL), "Test1");
        Assertions.assertTrue((Boolean) values.get(0).get(User.EMAIL_VERIFIED));
    }
}