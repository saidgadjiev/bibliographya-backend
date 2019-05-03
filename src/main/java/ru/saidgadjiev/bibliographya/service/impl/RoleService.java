package ru.saidgadjiev.bibliographya.service.impl;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.dao.impl.RoleDao;
import ru.saidgadjiev.bibliographya.data.ClientQueryVisitor;
import ru.saidgadjiev.bibliographya.data.PreparedSetter;
import ru.saidgadjiev.bibliographya.data.mapper.RoleFieldsMapper;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.AndCondition;
import ru.saidgadjiev.bibliographya.domain.Role;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoleService {

    private final RoleDao roleDao;

    public RoleService(RoleDao roleDao) {
        this.roleDao = roleDao;
    }

    public List<Role> getRoles(String query) {
        AndCondition andCondition = new AndCondition();
        List<PreparedSetter> values = new ArrayList<>();

        if (StringUtils.isNotBlank(query)) {
            Node node = new RSQLParser().parse(query);

            ClientQueryVisitor<Void, Void> visitor = new ClientQueryVisitor<>(new RoleFieldsMapper());

            node.accept(visitor);
            andCondition = visitor.getCondition();
            values = visitor.getValues();
        }

        return roleDao.getRoles(andCondition, values);
    }

    public int deleteRole(String role) {
        return roleDao.deleteRole(role);
    }

    public int createRole(String role) {
        return roleDao.createRole(new Role(role));
    }
}
