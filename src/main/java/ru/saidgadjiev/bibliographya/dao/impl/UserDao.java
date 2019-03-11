package ru.saidgadjiev.bibliographya.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.domain.UsersStats;
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
public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int update(List<UpdateValue> values, Collection<FilterCriteria> criteria) {
        String clause = FilterUtils.toClause(criteria, null);
        StringBuilder sql = new StringBuilder();

        sql.append("UPDATE user SET ");

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
                    PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO \"user\"(email, email_verified, password) VALUES (?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS
                    );

                    ps.setString(1, user.getUsername());
                    ps.setBoolean(2, user.isEmailVerified());
                    ps.setString(3, user.getPassword());

                    return ps;
                },
                keyHolderUser
        );
        Map<String, Object> keys = keyHolderUser.getKeys();

        if (keys != null && keys.containsKey("id")) {
            user.setId(((Number) keys.get("id")).intValue());
        }

        return user;
    }

    public User get(Collection<FilterCriteria> userCriteria, Collection<FilterCriteria> userAccountCriteria) {
        String userClause = FilterUtils.toClause(userCriteria, "u");
        String userAccountClause = FilterUtils.toClause(userAccountCriteria, "ua");

        StringBuilder sql = new StringBuilder();

        sql
                .append("SELECT ").append(selectList()).append(" ")
                .append("FROM \"user\" u INNER JOIN biography b ON u.id = b.user_id WHERE 1 = 1 ");

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
                "SELECT COUNT(*) as cnt FROM \"user\" WHERE email = ? AND email_verified = ?",
                preparedStatement -> {
                    preparedStatement.setString(1, email);
                    preparedStatement.setBoolean(2, true);
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

    public List<User> getUsers(Integer limit, Long offset, Collection<FilterCriteria> roleCriteria) {
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT ").append(selectList()).append(" FROM \"user\" u ");
        sql.append("INNER JOIN biography ba ON u.id = ba.user_id ");

        String clause = FilterUtils.toClause(roleCriteria, null);

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

                    for (FilterCriteria criterion: roleCriteria) {
                        criterion.getValueSetter().set(ps, ++i, criterion.getFilterValue());
                    }
                },
                (resultSet, i) -> map(resultSet)
        );
    }

    private User map(ResultSet rs) throws SQLException {
        User user = new User();

        user.setId(rs.getInt("u_id"));

        user.setEmailVerified(rs.getBoolean("u_verified"));
        user.setEmail(rs.getString("u_email"));
        user.setPassword(rs.getString("u_password"));

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
                "  u.email_verified as u_verified,\n" +
                "  u.email AS u_email,\n" +
                "  u.password AS u_password,\n" +
                "  b.id AS b_id,\n" +
                "  b.first_name AS b_first_name,\n" +
                "  b.last_name AS b_last_name\n";
    }
}
