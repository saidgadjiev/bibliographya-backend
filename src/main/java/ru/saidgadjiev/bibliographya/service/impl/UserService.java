package ru.saidgadjiev.bibliographya.service.impl;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.dao.impl.UserAccountDao;
import ru.saidgadjiev.bibliographya.dao.impl.UserRoleDao;
import ru.saidgadjiev.bibliographya.data.ClientQueryVisitor;
import ru.saidgadjiev.bibliographya.data.PreparedSetter;
import ru.saidgadjiev.bibliographya.data.mapper.GetUsersFieldsMapper;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.AndCondition;
import ru.saidgadjiev.bibliographya.domain.Role;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.domain.UsersStats;
import ru.saidgadjiev.bibliographya.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliographya.security.event.AddRoleEvent;
import ru.saidgadjiev.bibliographya.security.event.DeleteRoleEvent;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserAccountDao userDao;

    private final UserRoleDao userRoleDao;

    private ApplicationEventPublisher eventPublisher;

    public UserService(UserAccountDao userDao, UserRoleDao userRoleDao, ApplicationEventPublisher eventPublisher) {
        this.userDao = userDao;
        this.userRoleDao = userRoleDao;
        this.eventPublisher = eventPublisher;
    }

    public Page<User> getUsers(OffsetLimitPageRequest pageRequest, String roleQuery) {
        AndCondition andCondition = new AndCondition();
        List<PreparedSetter> values = new ArrayList<>();

        if (StringUtils.isNotBlank(roleQuery)) {
            Node node = new RSQLParser().parse(roleQuery);
            ClientQueryVisitor<Void, Void> visitor = new ClientQueryVisitor<>(new GetUsersFieldsMapper());

            node.accept(visitor);
            andCondition = visitor.getCondition();
            values = visitor.getValues();
        }

        List<User> userList = userDao.getUsers(pageRequest.getPageSize(), pageRequest.getOffset(), andCondition, values);
        Collection<Integer> userIds = userList.stream().map(User::getId).collect(Collectors.toList());
        Map<Integer, Set<Role>> roles = userRoleDao.getRoles(userIds);

        for (User user: userList) {
            user.setRoles(roles.get(user.getId()));
        }

        return new PageImpl<>(userList);
    }

    public int addRole(int userId, String role) {
        Role r = new Role(role);

        int added = userRoleDao.addRole(userId, r);

        eventPublisher.publishEvent(new AddRoleEvent(r, userId));

        return added;
    }

    public int deleteRole(int userId, String role) {
        Role r = new Role(role);

        int deleted = userRoleDao.deleteRole(userId, r);

        eventPublisher.publishEvent(new DeleteRoleEvent(r, userId));

        return deleted;
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
