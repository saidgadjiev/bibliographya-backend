package ru.saidgadjiev.bibliographya.dao.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.FilterOperation;
import ru.saidgadjiev.bibliographya.domain.Role;
import ru.saidgadjiev.bibliographya.utils.TableUtils;

import java.sql.PreparedStatement;
import java.util.*;

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
        Assertions.assertEquals(roleDao.getRoles(Collections.emptyList()).size(), 0);

        jdbcTemplate.update(
                "INSERT INTO role(name) VALUES('ROLE_ADMIN'), ('ROLE_TEST')"
        );

        List<Role> roleList = roleDao.getRoles(Collections.emptyList());

        Assertions.assertIterableEquals(Arrays.asList(new Role("ROLE_ADMIN"), new Role("ROLE_TEST")), roleList);

        Collection<FilterCriteria> criteria = new ArrayList<>();

        criteria.add(
                new FilterCriteria.Builder<String>()
                .propertyName("name")
                .filterValue("ROLE_ADMIN")
                .filterOperation(FilterOperation.EQ)
                .valueSetter(PreparedStatement::setString)
                .needPreparedSet(true)
                .build()
        );

        List<Role> filteredRoles = roleDao.getRoles(criteria);

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