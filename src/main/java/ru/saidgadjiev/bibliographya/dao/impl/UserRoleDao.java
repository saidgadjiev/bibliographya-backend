package ru.saidgadjiev.bibliographya.dao.impl;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliographya.domain.Role;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Repository
public class UserRoleDao {

    private final JdbcTemplate jdbcTemplate;

    public UserRoleDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int[] addRoles(int userId, Collection<Role> addRoles) {
        List<Role> roles = new ArrayList<>(addRoles);

        return jdbcTemplate.batchUpdate(
                "INSERT INTO user_role(user_id, role_name) VALUES(?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, userId);
                        ps.setString(2, roles.get(i).getName());
                    }

                    @Override
                    public int getBatchSize() {
                        return roles.size();
                    }
                });

    }

    public int addRole(int userId, Role role) {
        return jdbcTemplate.update(
                "INSERT INTO user_role(user_id, role_name) VALUES(?, ?)",
                preparedStatement -> {
                    preparedStatement.setInt(1, userId);
                    preparedStatement.setString(2, role.getName());
                }
        );
    }

    public int deleteRole(int userId, Role role) {
        return jdbcTemplate.update(
                "DELETE FROM user_role WHERE user_id = ? AND role_name = ?",
                preparedStatement -> {
                    preparedStatement.setInt(1, userId);
                    preparedStatement.setString(2, role.getName());
                }
        );
    }


    public Set<Role> getRoles(int userId) {
        Set<Role> roles = new LinkedHashSet<>();

        jdbcTemplate.query(
                "SELECT * FROM user_role WHERE user_id = ?",
                ps -> ps.setInt(1, userId),
                rs -> {
                    roles.add(new Role(rs.getString("role_name")));
                }
        );

        return roles;
    }
}

