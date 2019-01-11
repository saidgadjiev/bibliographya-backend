package ru.saidgadjiev.bibliography.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliography.dao.impl.UserDao;
import ru.saidgadjiev.bibliography.dao.impl.UserRoleDao;
import ru.saidgadjiev.bibliography.domain.User;
import ru.saidgadjiev.bibliography.model.OffsetLimitPageRequest;

import java.util.List;

@Service
public class UserService {

    private final UserDao userDao;

    private final UserRoleDao userRoleDao;

    public UserService(UserDao userDao, UserRoleDao userRoleDao) {
        this.userDao = userDao;
        this.userRoleDao = userRoleDao;
    }

    public Page<User> getUsers(OffsetLimitPageRequest pageRequest) {
        List<User> userList = userDao.getUsers(pageRequest.getPageSize(), pageRequest.getOffset());

        return new PageImpl<>(userList);
    }

    public int addRole(int userId, String role) {
        return userRoleDao.addRole(userId, role);
    }

    public int deleteRole(int userId, String role) {
        return userRoleDao.deleteRole(userId, role);
    }
}
