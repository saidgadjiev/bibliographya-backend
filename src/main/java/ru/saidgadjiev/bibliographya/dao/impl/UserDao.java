package ru.saidgadjiev.bibliographya.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.Role;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.domain.UsersStats;
import ru.saidgadjiev.bibliographya.utils.FilterUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class UserDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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

        List<User> users = jdbcTemplate.query(
                sql.toString(),
                ps -> {
                    int i = 0;

                    for (FilterCriteria criterion: roleCriteria) {
                        criterion.getValueSetter().set(ps, ++i, criterion.getFilterValue());
                    }
                },
        (resultSet, i) -> map(resultSet)
        );

        Collection<Integer> ids = users.stream().map(User::getId).collect(Collectors.toList());
        Map<Integer, Set<Role>> roles = getRoles(ids);

        for (User user: users) {
            user.setRoles(roles.get(user.getId()));
        }

        return users;
    }

    public UsersStats getStats() {
        UsersStats usersStats = new UsersStats();

        Map<ProviderType, Integer> countByProviderType = jdbcTemplate.query(
                "SELECT provider_id, COUNT(provider_id) as cnt FROM \"user\" GROUP BY provider_id",
                resultSet -> {
                    Map<ProviderType, Integer> stats = new LinkedHashMap<>();

                    while (resultSet.next()) {
                        stats.put(ProviderType.fromId(resultSet.getString("provider_id")), resultSet.getInt("cnt"));
                    }

                    return stats;
                }
        );

        usersStats.setUsersByProvider(countByProviderType);
        usersStats.setCount(countByProviderType.values().stream().count());

        return usersStats;
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

    private User map(ResultSet rs) throws SQLException {
        User user = new User();

        user.setId(rs.getInt("u_id"));
        user.setProviderType(ProviderType.fromId(rs.getString("u_provider_id")));
        user.setDeleted(rs.getBoolean("u_deleted"));

        Biography biography = new Biography();

        biography.setId(rs.getInt("ba_id"));
        biography.setFirstName(rs.getString("ba_first_name"));
        biography.setLastName(rs.getString("ba_last_name"));
        biography.setUserId(user.getId());

        user.setBiography(biography);

        return user;
    }


    private Map<Integer, Set<Role>> getRoles(Collection<Integer> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Integer, Set<Role>> roles = new HashMap<>();
        String inClause = userIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        jdbcTemplate.query(
                "SELECT * FROM user_role WHERE user_id IN (" + inClause + ") ",
                rs -> {
                    int userId = rs.getInt("user_id");

                    roles.putIfAbsent(userId, new LinkedHashSet<>());

                    roles.get(userId).add(new Role(rs.getString("role_name")));
                }
        );

        return roles;
    }

    private String selectList() {
        StringBuilder selectList = new StringBuilder();

        selectList
                .append("u.id as u_id,")
                .append("u.deleted as u_deleted,")
                .append("u.provider_id as u_provider_id,")
                .append("ba.id as ba_id,")
                .append("ba.first_name as ba_first_name,")
                .append("ba.last_name as ba_last_name");

        return selectList.toString();
    }
}
