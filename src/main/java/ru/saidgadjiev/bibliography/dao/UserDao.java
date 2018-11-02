package ru.saidgadjiev.bibliography.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliography.domain.Role;
import ru.saidgadjiev.bibliography.domain.User;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by said on 22.10.2018.
 */
@Repository
public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(User user) {
        String name = user.getName() == null ? "NULL" : "'" + user.getName() + "'";
        String password = user.getPassword() == null ? "NULL" : "'" + user.getPassword() + "'";

        jdbcTemplate.execute("INSERT INTO \"user\"(\"name\", \"password\") VALUES(" + name + ", " + password + ")");

        List<Role> roles = new ArrayList<>(user.getRoles());

        jdbcTemplate.batchUpdate(
                "INSERT INTO user_role(\"user_name\", \"role_name\") VALUES(?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, user.getName());
                        ps.setString(2, roles.get(i).getName());
                    }

                    @Override
                    public int getBatchSize() {
                        return roles.size();
                    }
                });
    }

    public User getByUsername(String username) {
        return jdbcTemplate.query(
                "SELECT " +
                        "u.name       as u_name, " +
                        "u.password   as u_password, " +
                        "ur.role_name as ur_role " +
                        "FROM \"user\" u LEFT JOIN user_role ur ON u.name = ur.user_name " +
                        "WHERE \"name\" = '" + username + "'",
                rs -> {
                    if (rs.next()) {
                        String username1 = rs.getString("u_name");
                        String password = rs.getString("u_password");
                        Set<Role> roles = new HashSet<>();

                        do {
                            roles.add(new Role(rs.getString("ur_role")));
                        } while (rs.next());

                        return new User(username1, password, roles);
                    }

                    return null;
                }
        );
    }
}
