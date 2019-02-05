package ru.saidgadjiev.bibliographya.dao.impl;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class RoleDaoTest {

    @Autowired
    private RoleDao roleDao;

    @org.junit.jupiter.api.Test
    void getRoles() {
        roleDao.getRoles(null);
    }

    @org.junit.jupiter.api.Test
    void deleteRole() {
    }

    @org.junit.jupiter.api.Test
    void createRole() {
    }
}