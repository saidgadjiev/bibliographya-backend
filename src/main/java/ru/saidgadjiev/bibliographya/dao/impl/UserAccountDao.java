package ru.saidgadjiev.bibliographya.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.domain.UserAccount;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

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

    public int updatePassword(String email, String password) {
        return jdbcTemplate.update(
                "UPDATE user_account SET password = ? WHERE email = ?",
                preparedStatement -> {
                    preparedStatement.setString(1, password);
                    preparedStatement.setString(2, email);
                }
        );
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
        Map<String, Object> keys = keyHolderUser.getKeys();

        if (keys != null && keys.containsKey("id")) {
            user.setId(((Number) keys.get("id")).intValue());
        }

        user.getUserAccount().setUserId(user.getId());

        KeyHolder keyHolderUserAccount = new GeneratedKeyHolder();

        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO user_account(email, password, user_id) VALUES(?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS
                    );

                    ps.setString(1, user.getUsername());
                    ps.setString(2, user.getPassword());
                    ps.setInt(3, user.getId());

                    return ps;
                },
                keyHolderUserAccount
        );
        keys = keyHolderUserAccount.getKeys();

        if (keys != null && keys.containsKey("id")) {
            user.getUserAccount().setId(((Number) keys.get("id")).intValue());
        }

        return user;
    }

    public User getByEmail(String email) {
        return jdbcTemplate.query(
                "SELECT\n" +
                        "  u.id AS u_id,\n" +
                        "  u.provider_id AS u_provider_id,\n" +
                        "  ua.id AS ua_id,\n" +
                        "  ua.email AS ua_email,\n" +
                        "  ua.password AS ua_password,\n" +
                        "  b.id AS b_id,\n" +
                        "  b.first_name AS b_first_name,\n" +
                        "  b.last_name AS b_last_name\n" +
                        "FROM \"user\" u INNER JOIN user_account ua ON ua.user_id = u.id INNER JOIN biography b ON u.id = b.user_id \n" +
                        "WHERE ua.email = ?",
                ps -> ps.setString(1, email),
                rs -> {
                    if (rs.next()) {
                        return map(rs);
                    }

                    return null;
                }
        );
    }

    public User getByUserId(int userId) {
        return jdbcTemplate.query(
                "SELECT\n" +
                        "  u.id AS u_id,\n" +
                        "  u.provider_id AS u_provider_id,\n" +
                        "  ua.id AS ua_id,\n" +
                        "  ua.email AS ua_email,\n" +
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
    }

    public boolean isExistEmail(String email) {
        return jdbcTemplate.query(
                "SELECT COUNT(*) as cnt FROM user_account WHERE email ='" + email + "'",
                rs -> rs.next() && rs.getLong("cnt") > 0
        );
    }

    private User map(ResultSet rs) throws SQLException {
        User user = new User();

        user.setId(rs.getInt("u_id"));
        user.setProviderType(ProviderType.fromId(rs.getString("u_provider_id")));

        UserAccount userAccount = new UserAccount();

        userAccount.setId(rs.getInt("ua_id"));
        userAccount.setEmail(rs.getString("ua_email"));
        userAccount.setPassword(rs.getString("ua_password"));
        userAccount.setUserId(user.getId());

        user.setUserAccount(userAccount);

        Biography biography = new Biography();

        biography.setId(rs.getInt("b_id"));
        biography.setFirstName(rs.getString("b_first_name"));
        biography.setLastName(rs.getString("b_last_name"));

        user.setBiography(biography);

        return user;
    }
}
