package ru.saidgadjiev.bibliography.service.impl;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliography.dao.impl.RoleDao;
import ru.saidgadjiev.bibliography.data.FilterCriteria;
import ru.saidgadjiev.bibliography.data.FilterCriteriaVisitor;
import ru.saidgadjiev.bibliography.domain.Role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class RoleService {

    private final RoleDao roleDao;

    public RoleService(RoleDao roleDao) {
        this.roleDao = roleDao;
    }

    public List<Role> getRoles(String query) {
        List<FilterCriteria> criteria = new ArrayList<>();

        if (StringUtils.isNotBlank(query)) {
            Node node = new RSQLParser().parse(query);

            node.accept(new FilterCriteriaVisitor<>(criteria, new HashMap<String, FilterCriteriaVisitor.Type>() {{
                put("name", FilterCriteriaVisitor.Type.STRING);
            }}));
        }

        return roleDao.getRoles(criteria);
    }

    public int deleteRole(String role) {
        return roleDao.deleteRole(role);
    }

    public int createRole(String role) {
        return roleDao.createRole(new Role(role));
    }
}
