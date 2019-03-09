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
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.domain.Role;

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
        createTables();
        createRole("ROLE_ADMIN");
        createUser(ProviderType.EMAIL_PASSWORD);
    }

    @AfterEach
    void after() {
        deleteTables();
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
                "CREATE TABLE IF NOT EXISTS role (\n" +
                        "  id SERIAL PRIMARY KEY,\n" +
                        "  name VARCHAR(255) NOT NULL UNIQUE\n" +
                        ")"
        );

        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS user_role (\n" +
                        "  id SERIAL PRIMARY KEY,\n" +
                        "  user_id INTEGER NOT NULL REFERENCES \"user\"(id),\n" +
                        "  role_name VARCHAR (255) NOT NULL REFERENCES role(name),\n" +
                        "  UNIQUE (user_id, role_name)\n" +
                        ")"
        );
    }

    private void deleteTables() {
        jdbcTemplate.execute(
                "DROP TABLE IF EXISTS user_role"
        );

        jdbcTemplate.execute(
                "DROP TABLE IF EXISTS role"
        );

        jdbcTemplate.execute(
                "DROP TABLE IF EXISTS \"user\""
        );
    }

    private void createRole(String role) {
        jdbcTemplate.update("INSERT INTO role(name) VALUES('" + role + "')");
    }

    private void createUser(ProviderType providerType) {
        jdbcTemplate.update(
                "INSERT INTO \"user\"(provider_id) VALUES('" + providerType.getId() + "')"
        );
    }
}