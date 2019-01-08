package ru.saidgadjiev.bibliography.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliography.auth.common.ProviderType;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.Role;
import ru.saidgadjiev.bibliography.domain.User;
import ru.saidgadjiev.bibliography.domain.UserAccount;

import java.sql.*;
import java.util.*;

/**
 * Created by said on 22.10.2018.
 */
@Repository
public class UserAccountDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserAccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User save(User user) {
        KeyHolder keyHolderUser = new GeneratedKeyHolder();

        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO \"user\"(provider_id) VALUES (?)", Statement.RETURN_GENERATED_KEYS);

                    ps.setString(1, user.getProviderType().getId());

                    return ps;
                },
                keyHolderUser
        );
        user.setId(((Number) keyHolderUser.getKeys().get("id")).intValue());

        user.getUserAccount().setUserId(user.getId());

        KeyHolder keyHolderUserAccount = new GeneratedKeyHolder();

        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO user_account(name, password, user_id) VALUES(?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS
                    );

                    if (user.getUsername() == null) {
                        ps.setNull(1, Types.VARCHAR);
                    } else {
                        ps.setString(1, user.getUsername());
                    }
                    if (user.getPassword() == null) {
                        ps.setNull(2, Types.VARCHAR);
                    } else {
                        ps.setString(2, user.getPassword());
                    }
                    ps.setInt(3, user.getId());

                    return ps;
                },
                keyHolderUserAccount
        );
        user.getUserAccount().setId(((Number) keyHolderUserAccount.getKeys().get("id")).intValue());

        List<Role> roles = new ArrayList<>(user.getRoles());

        jdbcTemplate.batchUpdate(
                "INSERT INTO user_role(user_id, role_name) VALUES(?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, user.getId());
                        ps.setString(2, roles.get(i).getName());
                    }

                    @Override
                    public int getBatchSize() {
                        return roles.size();
                    }
                });

        return user;
    }

    public User getByUsername(String name) {
        User result = jdbcTemplate.query(
                "SELECT\n" +
                        "  u.id AS u_id,\n" +
                        "  u.provider_id AS u_provider_id,\n" +
                        "  ua.id AS ua_id,\n" +
                        "  ua.name AS ua_name,\n" +
                        "  ua.password AS ua_password,\n" +
                        "  b.id AS b_id,\n" +
                        "  b.first_name AS b_first_name,\n" +
                        "  b.last_name AS b_last_name\n" +
                        "FROM \"user\" u INNER JOIN user_account ua ON ua.user_id = u.id INNER JOIN biography b ON u.id = b.user_id \n" +
                        "WHERE ua.name = ?",
                ps -> ps.setString(1, name),
                rs -> {
                    if (rs.next()) {
                        return map(rs);
                    }

                    return null;
                }
        );

        if (result == null) {
            return null;
        }

        result.setRoles(getRoles(result.getId()));

        return result;
    }

    public User getById(int userId) {
        User result = jdbcTemplate.query(
                "SELECT\n" +
                        "  u.id AS u_id,\n" +
                        "  u.provider_id AS u_provider_id,\n" +
                        "  ua.id AS ua_id,\n" +
                        "  ua.name AS ua_name,\n" +
                        "  ua.password AS ua_password,\n" +
                        "  b.id AS b_id,\n" +
                        "  b.first_name AS b_first_name,\n" +
                        "  b.last_name AS b_last_name\n" +
                        "FROM \"user\" u INNER JOIN user_account ua ON ua.user_id = u.id INNER JOIN biography b ON u.id = b.user_id \n" +
                        "WHERE u.id = ?",
                ps -> ps.setInt(1, userId),
                rs -> {
                    if (rs.next()) {
                        return map(rs);
                    }

                    return null;
                }
        );

        if (result == null) {
            return null;
        }

        result.setRoles(getRoles(userId));

        return result;
    }

    public boolean isExistUsername(String username) {
        return jdbcTemplate.query(
                "SELECT COUNT(*) as cnt FROM user_account WHERE name ='" + username + "'",
                rs -> {
                    if (rs.next()) {
                        return rs.getLong("cnt") > 0;
                    }

                    return false;
                }
        );
    }

    private Set<Role> getRoles(int userId) {
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

    private User map(ResultSet rs) throws SQLException {
        User user = new User();

        user.setId(rs.getInt("u_id"));
        user.setProviderType(ProviderType.fromId(rs.getString("u_provider_id")));

        UserAccount userAccount = new UserAccount();

        userAccount.setId(rs.getInt("ua_id"));
        userAccount.setName(rs.getString("ua_name"));
        userAccount.setPassword(rs.getString("ua_password"));

        user.setUserAccount(userAccount);

        Biography biography = new Biography();

        biography.setId(rs.getInt("b_id"));
        biography.setFirstName(rs.getString("b_first_name"));
        biography.setLastName(rs.getString("b_last_name"));

        user.setBiography(biography);

        return user;
    }
}
