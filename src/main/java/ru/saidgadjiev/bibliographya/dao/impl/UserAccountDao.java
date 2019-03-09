package ru.saidgadjiev.bibliographya.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.domain.UserAccount;
import ru.saidgadjiev.bibliographya.utils.FilterUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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

    public int update(List<UpdateValue> values, Collection<FilterCriteria> criteria) {
        String clause = FilterUtils.toClause(criteria, null);
        StringBuilder sql = new StringBuilder();

        sql.append("UPDATE user_account SET ");

        for (Iterator<UpdateValue> iterator = values.iterator(); iterator.hasNext(); ) {
            sql.append(iterator.next().getName()).append(" = ?");

            if (iterator.hasNext()) {
                sql.append(", ");
            }
        }

        if (StringUtils.isNotBlank(clause)) {
            sql.append(" WHERE ").append(clause);
        }

        return jdbcTemplate.update(
                sql.toString(),
                ps -> {
                    int i = 0;

                    for (UpdateValue updateValue : values) {
                        updateValue.getSetter().set(ps, ++i, updateValue.getValue());
                    }
                    for (FilterCriteria criterion : criteria) {
                        if (criterion.isNeedPreparedSet()) {
                            criterion.getValueSetter().set(ps, ++i, criterion.getFilterValue());
                        }
                    }
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
                            "INSERT INTO user_account(email, email_verified, password, user_id) VALUES(?, ?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS
                    );

                    ps.setString(1, user.getUsername());
                    ps.setBoolean(2, user.getUserAccount().isEmailVerified());
                    ps.setString(3, user.getPassword());
                    ps.setInt(4, user.getId());

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

    public User get(Collection<FilterCriteria> userCriteria, Collection<FilterCriteria> userAccountCriteria) {
        String userClause = FilterUtils.toClause(userCriteria, "u");
        String userAccountClause = FilterUtils.toClause(userAccountCriteria, "ua");

        StringBuilder sql = new StringBuilder();

        sql
                .append("SELECT ").append(selectList()).append(" ")
                .append("FROM \"user\" u INNER JOIN user_account ua ON ua.user_id = u.id INNER JOIN biography b ON u.id = b.user_id WHERE 1 = 1 ");

        if (StringUtils.isNotBlank(userClause)) {
            sql.append("AND ").append(userClause).append(" ");
        }
        if (StringUtils.isNotBlank(userAccountClause)) {
            sql.append("AND ").append(userAccountClause).append(" ");
        }

        return jdbcTemplate.query(
                sql.toString(),
                ps -> {
                    int i = 0;

                    for (FilterCriteria criterion : userCriteria) {
                        if (criterion.isNeedPreparedSet()) {
                            criterion.getValueSetter().set(ps, ++i, criterion.getFilterValue());
                        }
                    }

                    for (FilterCriteria criterion : userAccountCriteria) {
                        if (criterion.isNeedPreparedSet()) {
                            criterion.getValueSetter().set(ps, ++i, criterion.getFilterValue());
                        }
                    }
                },
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
                "SELECT COUNT(*) as cnt FROM user_account WHERE email = ? AND email_verified = ?",
                preparedStatement -> {
                    preparedStatement.setString(1, email);
                    preparedStatement.setBoolean(2, true);
                },
                rs -> rs.next() && rs.getLong("cnt") > 0
        );
    }

    private User map(ResultSet rs) throws SQLException {
        User user = new User();

        user.setId(rs.getInt("u_id"));
        user.setProviderType(ProviderType.fromId(rs.getString("u_provider_id")));

        UserAccount userAccount = new UserAccount();

        userAccount.setId(rs.getInt("ua_id"));
        userAccount.setEmailVerified(rs.getBoolean("ua_verified"));
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

    private String selectList() {
        return "  u.id AS u_id,\n" +
                "  u.provider_id AS u_provider_id,\n" +
                "  ua.id AS ua_id,\n" +
                "  ua.email_verified as ua_verified,\n" +
                "  ua.email AS ua_email,\n" +
                "  ua.password AS ua_password,\n" +
                "  b.id AS b_id,\n" +
                "  b.first_name AS b_first_name,\n" +
                "  b.last_name AS b_last_name\n";
    }
}
