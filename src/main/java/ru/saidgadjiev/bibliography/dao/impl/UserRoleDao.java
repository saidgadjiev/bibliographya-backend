package ru.saidgadjiev.bibliography.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@Repository
public class UserRoleDao {

    private final JdbcTemplate jdbcTemplate;

    public UserRoleDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int addRole(int userId, String role) {
        return jdbcTemplate.update(
                "INSERT INTO user_role(user_id, role_name) VALUES(?, ?)",
                new PreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement preparedStatement) throws SQLException {
                        preparedStatement.setInt(1, userId);
                        preparedStatement.setString(2, role);
                    }
                }
        );
    }

    public int deleteRole(int userId, String role) {
        return jdbcTemplate.update(
                "DELETE FROM user_role WHERE user_id = ? AND role_name = ?",
                new PreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement preparedStatement) throws SQLException {
                        preparedStatement.setInt(1, userId);
                        preparedStatement.setString(2, role);
                    }
                }
        );
    }
}

