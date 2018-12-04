package ru.saidgadjiev.bibliography.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.Role;
import ru.saidgadjiev.bibliography.domain.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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

    public User getByUsername(String name) {
        User result = jdbcTemplate.query(
                "SELECT " +
                        "u.name       as u_name, " +
                        "u.password   as u_password, " +
                        "b.first_name as first_name, " +
                        "b.last_name as last_name, " +
                        "b.id as biography_id " +
                        "FROM \"user\" u INNER JOIN biography b ON u.name = b.user_name " +
                        "WHERE u.\"name\" = '" + name + "'",
                rs -> {
                    if (rs.next()) {
                        User user = new User();

                        Biography biography = new Biography();

                        biography.setId(rs.getInt("biography_id"));
                        biography.setFirstName(rs.getString("first_name"));
                        biography.setLastName(rs.getString("last_name"));

                        user.setName(rs.getString("u_name"));
                        user.setPassword(rs.getString("u_password"));
                        user.setBiography(biography);

                        return user;
                    }

                    return null;
                }
        );

        if (result == null) {
            return null;
        }

        Set<Role> roles = new LinkedHashSet<>();

        jdbcTemplate.query(
                "SELECT * FROM user_role WHERE user_name = '" + name + "'",
                rs -> {
                    roles.add(new Role(rs.getString("role_name")));
                }
        );

        result.setRoles(roles);

        return result;
    }

    public boolean isExistUsername(String username) {
        return jdbcTemplate.query(
                "SELECT COUNT(*) as cnt FROM \"user\" WHERE name='" + username + "'",
                new ResultSetExtractor<Boolean>() {
                    @Override
                    public Boolean extractData(ResultSet rs) throws SQLException, DataAccessException {
                        if (rs.next()) {
                            return rs.getLong("cnt") > 0;
                        }

                        return false;
                    }
                }
        );
    }
}
