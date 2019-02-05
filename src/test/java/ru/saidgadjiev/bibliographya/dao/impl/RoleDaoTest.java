package ru.saidgadjiev.bibliographya.dao.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.saidgadjiev.bibliographya.domain.Role;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class RoleDaoTest {

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void init() {
        createTableRole();
    }

    @org.junit.jupiter.api.Test
    void getRoles() {
        jdbcTemplate.update(
                "INSERT INTO role(name) VALUES('ROLE_ADMIN'), ('ROLE_TEST')"
        );

        List<Role> roleList = roleDao.getRoles(Collections.emptyList());

        Assertions.assertIterableEquals(Arrays.asList(new Role("ROLE_ADMIN"), new Role("ROLE_TEST")), roleList);
    }

    @org.junit.jupiter.api.Test
    void deleteRole() {
    }

    @org.junit.jupiter.api.Test
    void createRole() {
    }

    private void createTableRole() {
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS role (\n" +
                        "  id SERIAL PRIMARY KEY,\n" +
                        "  name VARCHAR(255) NOT NULL UNIQUE\n" +
                        ")"
        );
    }
}