package ru.saidgadjiev.bibliographya.service.impl;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.dao.impl.UserDao;
import ru.saidgadjiev.bibliographya.dao.impl.UserRoleDao;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.FilterCriteriaVisitor;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.domain.UsersStats;
import ru.saidgadjiev.bibliographya.model.OffsetLimitPageRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class UserService {

    private final UserDao userDao;

    private final UserRoleDao userRoleDao;

    public UserService(UserDao userDao, UserRoleDao userRoleDao) {
        this.userDao = userDao;
        this.userRoleDao = userRoleDao;
    }

    public Page<User> getUsers(OffsetLimitPageRequest pageRequest, String roleQuery) {
        List<FilterCriteria> roleCriteria = new ArrayList<>();

        if (StringUtils.isNotBlank(roleQuery)) {
            Node node = new RSQLParser().parse(roleQuery);

            node.accept(new FilterCriteriaVisitor<>(roleCriteria, new HashMap<String, FilterCriteriaVisitor.Type>() {{
                put("role_name", FilterCriteriaVisitor.Type.STRING);
            }}));
        }

        List<User> userList = userDao.getUsers(pageRequest.getPageSize(), pageRequest.getOffset(), roleCriteria);

        return new PageImpl<>(userList);
    }

    public int addRole(int userId, String role) {
        return userRoleDao.addRole(userId, role);
    }

    public int deleteRole(int userId, String role) {
        return userRoleDao.deleteRole(userId, role);
    }

    public int deleteUser(int userId) {
        return userDao.markDelete(userId, true);
    }

    public int restoreUser(int userId) {
        return userDao.markDelete(userId, false);
    }

    public UsersStats getStats() {
        return userDao.getStats();
    }
}
