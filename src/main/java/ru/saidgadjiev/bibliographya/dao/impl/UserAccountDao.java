package ru.saidgadjiev.bibliographya.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.dao.impl.dsl.DslVisitor;
import ru.saidgadjiev.bibliographya.data.PreparedSetter;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.column.ColumnSpec;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.AndCondition;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.Equals;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.Expression;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.literals.Param;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.domain.UserAccount;
import ru.saidgadjiev.bibliographya.domain.UsersStats;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
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
                            "INSERT INTO user_account(email, phone, password, user_id) VALUES(?, ?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS
                    );

                    ps.setString(1, user.getUserAccount().getEmail());
                    ps.setString(2, user.getUserAccount().getPhone());
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

    public User getByEmail(String email) {
        return getUniqueUser(new AndCondition() {{
            add(new Equals(new ColumnSpec(UserAccount.EMAIL), new Param()));
        }}, Collections.singletonList((preparedStatement, index) -> preparedStatement.setString(index, email)));
    }

    public User getByPhone(String phone) {
        return getUniqueUser(new AndCondition() {{
            add(new Equals(new ColumnSpec(User.PHONE), new Param()));
        }}, Collections.singletonList((preparedStatement, index) -> preparedStatement.setString(index, phone)));
    }

    public User getUniqueUser(AndCondition andCondition, List<PreparedSetter> values) {
        List<User> users = getUsers(andCondition, values);

        return users.isEmpty() ? null : users.iterator().next();
    }

    public List<User> getUsers(AndCondition andCondition, List<PreparedSetter> values) {
        DslVisitor visitor = new DslVisitor("u");

        new Expression() {{
            add(andCondition);
        }}.accept(visitor);

        String userClause = visitor.getClause();

        StringBuilder sql = new StringBuilder();

        sql
                .append("SELECT ").append(selectList()).append(" ")
                .append(" FROM \"user\" u INNER JOIN user_account ua ON ua.user_id = u.id INNER JOIN biography b ON u.id = b.user_id ");

        if (StringUtils.isNotBlank(userClause)) {
            sql.append("WHERE ").append(userClause).append(" ");
        }

        return jdbcTemplate.query(
                sql.toString(),
                ps -> {
                    int i = 0;

                    for (PreparedSetter preparedSetter: values) {
                        preparedSetter.set(ps, ++i);
                    }
                },
                (rs, rowNum) -> map(rs)
        );
    }

    public boolean isExistEmail(String email) {
        return jdbcTemplate.query(
                "SELECT COUNT(*) as cnt FROM \"user_account\" WHERE email = ?",
                preparedStatement -> {
                    preparedStatement.setString(1, email);
                },
                rs -> rs.next() && rs.getLong("cnt") > 0
        );
    }

    public boolean isExistPhone(String phone) {
        return jdbcTemplate.query(
                "SELECT COUNT(*) as cnt FROM \"user_account\" WHERE phone = ?",
                preparedStatement -> {
                    preparedStatement.setString(1, phone);
                },
                rs -> rs.next() && rs.getLong("cnt") > 0
        );
    }

    public UsersStats getStats() {
        return jdbcTemplate.query(
                "SELECT COUNT(*) as cnt FROM \"user\"",
                resultSet -> {
                    if (resultSet.next()) {
                        UsersStats stats = new UsersStats();

                        stats.setCount(resultSet.getLong("cnt"));

                        return stats;
                    }

                    return null;
                }
        );
    }

    public int markDelete(int id, boolean deleted) {
        return jdbcTemplate.update(
                "UPDATE \"user\" SET deleted = ? WHERE id = ?",
                preparedStatement -> {
                    preparedStatement.setBoolean(1, deleted);
                    preparedStatement.setInt(2, id);
                }
        );
    }

    public List<User> getUsers(Integer limit, Long offset, AndCondition roleCriteria, List<PreparedSetter> values) {
        StringBuilder sql = new StringBuilder();

        sql
                .append("SELECT ").append(selectList())
                .append(" FROM \"user\" u INNER JOIN user_account ua ON ua.user_id = u.id INNER JOIN biography b ON u.id = b.user_id ");

        DslVisitor dslVisitor = new DslVisitor(null);

        new Expression() {{
            add(roleCriteria);
        }}.accept(dslVisitor);

        String clause = dslVisitor.getClause();

        if (StringUtils.isNotBlank(clause)) {
            sql.append(" WHERE ").append("u.id IN (SELECT user_id FROM user_role WHERE ").append(clause).append(")");
        }
        sql.append(" ORDER BY u.id ");
        if (limit != null) {
            sql.append("LIMIT ").append(limit).append(" ");
        }
        if (offset != null) {
            sql.append("OFFSET ").append(offset);
        }

        return jdbcTemplate.query(
                sql.toString(),
                ps -> {
                    int i = 0;

                    for (PreparedSetter preparedSetter: values) {
                        preparedSetter.set(ps, ++i);
                    }
                },
                (resultSet, i) -> map(resultSet)
        );
    }

    private User map(ResultSet rs) throws SQLException {
        User user = new User();

        user.setId(rs.getInt("u_id"));

        UserAccount userAccount = new UserAccount();

        userAccount.setEmail(rs.getString("u_email"));
        userAccount.setPhone(rs.getString("u_phone"));
        userAccount.setPassword(rs.getString("u_password"));

        user.setUserAccount(userAccount);

        user.setProviderType(ProviderType.fromId(rs.getString("u_provider_id")));

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
                "  ua.email AS ua_email,\n" +
                "  ua.phone AS ua_phone,\n" +
                "  ua.password AS ua_password,\n" +
                "  b.id AS b_id,\n" +
                "  b.first_name AS b_first_name,\n" +
                "  b.last_name AS b_last_name\n";
    }
}
