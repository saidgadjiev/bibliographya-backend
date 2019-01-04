package ru.saidgadjiev.bibliography.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliography.auth.common.ProviderType;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.Role;
import ru.saidgadjiev.bibliography.domain.SocialAccount;
import ru.saidgadjiev.bibliography.domain.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by said on 25.12.2018.
 */
@Repository
public class SocialAccountDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public SocialAccountDao(JdbcTemplate jdbcTemplate) {
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

        user.getSocialAccount().setUserId(user.getId());

        KeyHolder keyHolderSocialAccount = new GeneratedKeyHolder();

        jdbcTemplate.update(
                con -> {
                    PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO social_account(account_id, user_id) VALUES(?, ?)",
                            Statement.RETURN_GENERATED_KEYS
                    );
                    ps.setString(1, user.getSocialAccount().getAccountId());
                    ps.setInt(2, user.getId());

                    return ps;
                },
                keyHolderSocialAccount
        );
        user.getSocialAccount().setId(((Number) keyHolderSocialAccount.getKeys().get("id")).intValue());

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

    public User getByUserId(int userId) {
        User result = jdbcTemplate.query(
                "SELECT\n" +
                        "  u.id AS u_id,\n" +
                        "  u.provider_id AS u_provider_id,\n" +
                        "  sa.id AS sa_id,\n" +
                        "  sa.account_id AS sa_account_id,\n" +
                        "  sa.user_id AS sa_user_id,\n" +
                        "  b.id AS b_id,\n" +
                        "  b.first_name AS b_first_name,\n" +
                        "  b.last_name AS b_last_name\n" +
                        "FROM \"user\" u INNER JOIN social_account sa ON sa.user_id = u.id INNER JOIN biography b ON u.id = b.user_id \n" +
                        "WHERE u.id = ?",
                ps -> {
                    ps.setInt(1, userId);
                },
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

    public User getByAccountId(ProviderType providerType, String accountId) {
        User result = jdbcTemplate.query(
                "SELECT\n" +
                        "  u.id AS u_id,\n" +
                        "  u.provider_id AS u_provider_id,\n" +
                        "  sa.id AS sa_id,\n" +
                        "  sa.account_id AS sa_account_id,\n" +
                        "  sa.user_id AS sa_user_id,\n" +
                        "  b.id AS b_id,\n" +
                        "  b.first_name AS b_first_name,\n" +
                        "  b.last_name AS b_last_name\n" +
                        "FROM \"user\" u INNER JOIN social_account sa ON sa.user_id = u.id INNER JOIN biography b ON u.id = b.user_id \n" +
                        "WHERE u.provider_id = ? AND sa.account_id = ?",
                ps -> {
                    ps.setString(1, providerType.getId());
                    ps.setString(2, accountId);
                },
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

        SocialAccount socialAccount = new SocialAccount();

        socialAccount.setId(rs.getInt("sa_id"));
        socialAccount.setAccountId(rs.getString("sa_account_id"));
        socialAccount.setUserId(rs.getInt("sa_user_id"));

        user.setSocialAccount(socialAccount);

        Biography biography = new Biography();

        biography.setId(rs.getInt("b_id"));
        biography.setFirstName(rs.getString("b_first_name"));
        biography.setLastName(rs.getString("b_last_name"));

        user.setBiography(biography);

        return user;
    }
}
