package ru.saidgadjiev.bibliographya.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliographya.dao.impl.dsl.DslVisitor;
import ru.saidgadjiev.bibliographya.data.PreparedSetter;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.AndCondition;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.Expression;
import ru.saidgadjiev.bibliographya.domain.Role;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class RoleDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public RoleDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Role> getRoles(AndCondition andCondition, List<PreparedSetter> values) {
        StringBuilder sql = new StringBuilder("SELECT * FROM role");

        DslVisitor visitor = new DslVisitor(null);

        new Expression() {{
            add(andCondition);
        }}.accept(visitor);

        String clause = visitor.getClause();

        if (StringUtils.isNotBlank(clause)) {
            sql.append(" WHERE ").append(clause);
        }

        return jdbcTemplate.query(
                sql.toString(),
                preparedStatement -> {
                    int i = 0;

                    for (PreparedSetter preparedSetter: values) {
                        preparedSetter.set(preparedStatement, ++i);
                    }
                },
                (resultSet, i) -> map(resultSet)
        );
    }

    public int deleteRole(String role) {
        return jdbcTemplate.update(
                "DELETE FROM role WHERE name = ?",
                preparedStatement -> preparedStatement.setString(1, role)
        );
    }

    public int createRole(Role role) {
        return jdbcTemplate.update(
                "INSERT INTO role(name) VALUES (?)",
                preparedStatement -> preparedStatement.setString(1, role.getName())
        );
    }

    private Role map(ResultSet rs) throws SQLException {
        return new Role(rs.getString("name"));
    }
}
