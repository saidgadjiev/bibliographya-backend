package ru.saidgadjiev.bibliography.service.impl;

import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliography.dao.impl.RoleDao;
import ru.saidgadjiev.bibliography.domain.Role;

import java.util.List;

@Service
public class RoleService {

    private final RoleDao roleDao;

    public RoleService(RoleDao roleDao) {
        this.roleDao = roleDao;
    }

    public List<Role> getRoles() {
        return roleDao.getRoles();
    }

    public int deleteRole(String role) {
        return roleDao.deleteRole(role);
    }

    public int createRole(String role) {
        return roleDao.createRole(new Role(role));
    }
}
