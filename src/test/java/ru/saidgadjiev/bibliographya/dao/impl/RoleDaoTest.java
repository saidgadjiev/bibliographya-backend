package ru.saidgadjiev.bibliographya.dao.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.saidgadjiev.bibliographya.data.PreparedSetter;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.column.ColumnSpec;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.AndCondition;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.Equals;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.literals.Param;
import ru.saidgadjiev.bibliographya.domain.Role;
import ru.saidgadjiev.bibliographya.utils.TableUtils;

import java.util.ArrayList;
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
        TableUtils.createRoleTable(jdbcTemplate);
    }

    @AfterEach
    void after() {
        TableUtils.deleteTableRole(jdbcTemplate);
    }

    @org.junit.jupiter.api.Test
    void getRoles() {
        Assertions.assertEquals(roleDao.getRoles(new AndCondition(), Collections.emptyList()).size(), 0);

        jdbcTemplate.update(
                "INSERT INTO role(name) VALUES('ROLE_ADMIN'), ('ROLE_TEST')"
        );

        List<Role> roleList = roleDao.getRoles(new AndCondition(), Collections.emptyList());

        Assertions.assertIterableEquals(Arrays.asList(new Role("ROLE_ADMIN"), new Role("ROLE_TEST")), roleList);

        List<Role> filteredRoles = roleDao.getRoles(new AndCondition() {{
            add(new Equals(new ColumnSpec("name"), new Param()));
        }}, new ArrayList<PreparedSetter>() {{
            add((preparedStatement, index) -> preparedStatement.setString(index, Role.ROLE_ADMIN));
        }});

        Assertions.assertIterableEquals(Collections.singleton(new Role("ROLE_ADMIN")), filteredRoles);
    }

    @org.junit.jupiter.api.Test
    void deleteRole() {
        jdbcTemplate.update(
                "INSERT INTO role(name) VALUES('ROLE_ADMIN')"
        );

        Assertions.assertEquals(1, roleDao.deleteRole("ROLE_ADMIN"));
    }

    @org.junit.jupiter.api.Test
    void createRole() {
        Role role = new Role("ROLE_ADMIN");

        roleDao.createRole(role);

        List<Role> roles = jdbcTemplate.query(
                "SELECT * FROM role",
                (resultSet, i) -> new Role(resultSet.getString("name"))
        );

        Assertions.assertEquals(roles.get(0), role);
    }
}