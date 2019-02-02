package ru.saidgadjiev.bibliographya.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliographya.dao.api.BiographyLikeDao;
import ru.saidgadjiev.bibliographya.domain.BiographyLike;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by said on 15.11.2018.
 */
@Repository
@Qualifier("sql")
public class BiographyLikeDaoImpl implements BiographyLikeDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public BiographyLikeDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int create(BiographyLike like) {
        return jdbcTemplate.update(
                "INSERT INTO biography_like" +
                        "(\"user_id\", \"biography_id\") " +
                        "VALUES('" +
                        like.getUserId() + "','" +
                        like.getBiographyId() +
                        "') ON CONFLICT DO NOTHING"
        );
    }

    @Override
    public int delete(BiographyLike like) {
        return jdbcTemplate.update(
                "DELETE " +
                        "FROM biography_like " +
                        "WHERE user_id='" + like.getUserId() + "' AND biography_id=" + like.getBiographyId()
        );
    }

    @Override
    public int getLikesCount(int biographyId) {
        return jdbcTemplate.query(
                "SELECT COUNT(*) as cnt FROM biography_like WHERE biography_id=" + biographyId,
                new ResultSetExtractor<Integer>() {
                    @Override
                    public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
                        if (rs.next()) {
                            return rs.getInt("cnt");
                        }

                        return 0;
                    }
                }
        );
    }

    @Override
    public boolean isLiked(int userId, int biographyId) {
        return jdbcTemplate.query(
                "SELECT 1 FROM biography_like WHERE user_id ='" + userId + "' AND biography_id ='" + biographyId + "'",
                ResultSet::next
        );
    }

    @Override
    public Map<Integer, Boolean> isLikedByBiographies(int userId, Collection<Integer> biographiesIds) {
        StringBuilder sql = new StringBuilder();
        String inClause = biographiesIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        sql.append("SELECT biography_id FROM biography_like WHERE user_id =");
        sql.append(userId);
        sql.append(" AND biography_id IN (");
        sql.append(inClause);
        sql.append(")");

        return jdbcTemplate.query(
                sql.toString(),
                rs -> {
                    Map<Integer, Boolean> result = new HashMap<>();

                    while (rs.next()) {
                        result.put(rs.getInt("biography_id"), true);
                    }

                    biographiesIds
                            .stream()
                            .filter(integer -> !result.containsKey(integer))
                            .forEach(integer -> result.put(integer, false));

                    return result;
                }
        );
    }

    @Override
    public Map<Integer, Integer> getLikesCountByBiographies(Collection<Integer> biographiesIds) {
        if (biographiesIds.isEmpty()) {
            Map<Integer, Integer> result = new HashMap<>();

            biographiesIds.forEach(integer -> result.put(integer, 0));

            return result;
        }
        StringBuilder sql = new StringBuilder();
        String inClause = biographiesIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        sql.append("SELECT biography_id, COUNT(biography_id) FROM biography_like WHERE biography_id IN (");
        sql.append(inClause);
        sql.append(") GROUP BY biography_id");

        return jdbcTemplate.query(
                sql.toString(),
                rs -> {
                    Map<Integer, Integer> result = new HashMap<>();

                    while (rs.next()) {
                        result.put(rs.getInt(1), rs.getInt(2));
                    }
                    biographiesIds
                            .stream()
                            .filter(integer -> !result.containsKey(integer))
                            .forEach(integer -> result.put(integer, 0));

                    return result;
                }
        );
    }

    @Override
    public long countOff() {
        return jdbcTemplate.query(
                "SELECT COUNT(*) as cnt FROM biography_like",
                rs -> {
                    if (rs.next()) {
                        return rs.getLong("cnt");
                    }

                    return 0L;
                }
        );
    }
}
