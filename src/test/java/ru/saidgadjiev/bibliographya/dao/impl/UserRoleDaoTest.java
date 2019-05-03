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
import ru.saidgadjiev.bibliographya.domain.Role;
import ru.saidgadjiev.bibliographya.utils.TableUtils;

import java.util.Arrays;
import java.util.Set;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class UserRoleDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRoleDao userRoleDao;

    @BeforeEach
    void init() {
        TableUtils.createTableUser(jdbcTemplate);
        TableUtils.createRoleTable(jdbcTemplate);
        TableUtils.createUserRoleTable(jdbcTemplate);
        createRole("ROLE_ADMIN");
        createUser();
    }

    @AfterEach
    void after() {
        TableUtils.deleteTableUserRole(jdbcTemplate);
        TableUtils.deleteTableRole(jdbcTemplate);
        TableUtils.deleteTableUser(jdbcTemplate);
    }

    @Test
    void addRoles() {
        Assertions.assertTrue(userRoleDao.getRoles(1).isEmpty());

        userRoleDao.addRoles(1, Arrays.asList(new Role("ROLE_ADMIN")));

        Set<Role> roles = userRoleDao.getRoles(1);

        Assertions.assertIterableEquals(roles, Arrays.asList(new Role("ROLE_ADMIN")));
    }

    @Test
    void addRole() {
        Assertions.assertTrue(userRoleDao.getRoles(1).isEmpty());

        userRoleDao.addRole(1, new Role("ROLE_ADMIN"));

        Set<Role> roles = userRoleDao.getRoles(1);

        Assertions.assertEquals(roles.iterator().next(), new Role("ROLE_ADMIN"));
    }

    @Test
    void deleteRole() {
        Assertions.assertTrue(userRoleDao.getRoles(1).isEmpty());

        userRoleDao.addRole(1, new Role("ROLE_ADMIN"));

        Set<Role> roles = userRoleDao.getRoles(1);

        Assertions.assertFalse(roles.isEmpty());

        userRoleDao.deleteRole(1, new Role("ROLE_ADMIN"));

        Assertions.assertTrue(userRoleDao.getRoles(1).isEmpty());
    }

    private void createRole(String role) {
        jdbcTemplate.update("INSERT INTO role(name) VALUES('" + role + "')");
    }

    private void createUser() {
        jdbcTemplate.update(
                "INSERT INTO \"user\"(email, password, phone) VALUES(?, ?, ?)",
                preparedStatement -> {
                    preparedStatement.setString(1, "Test");
                    preparedStatement.setString(2, "Test");
                    preparedStatement.setString(3, "Test");
                }
        );
    }
}