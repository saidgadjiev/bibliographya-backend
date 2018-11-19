package ru.saidgadjiev.bibliography.dao;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.BiographyComment;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by said on 17.11.2018.
 */
@Repository
public class BiographyCommentDao {

    private final JdbcTemplate jdbcTemplate;

    public BiographyCommentDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int create(BiographyComment biographyComment) {
        return jdbcTemplate.update(
                "INSERT INTO biography_comment" +
                        "(\"content\", \"biography_id\", \"user_name\", \"parent_id\") " +
                        "VALUES(?, ?, ?, ?)",
                ps -> {
                    ps.setString(1, biographyComment.getContent());
                    ps.setInt(2, biographyComment.getBiographyId());
                    ps.setString(3, biographyComment.getUserName());

                    if (biographyComment.getParentId() != null) {
                        ps.setInt(4, biographyComment.getParentId());
                    }
                }
        );
    }

    public int delete(BiographyComment biographyComment) {
        return jdbcTemplate.update(
                "DELETE FROM biography_comment WHERE biography_id = ? AND user_name = ?",
                new PreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps) throws SQLException {
                        ps.setInt(1, biographyComment.getBiographyId());
                        ps.setString(2, biographyComment.getUserName());
                    }
                }
        );
    }

    public List<BiographyComment> getComments(int biographyId, int limit, long offset) {
        return jdbcTemplate.query(
                "SELECT\n" +
                        "  bc1.*,\n" +
                        "  bg1.first_name,\n" +
                        "  bg1.last_name,\n" +
                        "  bg2.user_name AS reply_user_name,\n" +
                        "  bg2.first_name AS reply_first_name\n" +
                        "FROM biography_comment bc1 LEFT JOIN biography_comment bc2 ON bc1.parent_id = bc2.id\n" +
                        "  LEFT JOIN biography bg1 ON bc1.user_name = bg1.user_name\n" +
                        "  LEFT JOIN biography bg2 ON bc2.user_name = bg2.user_name\n" +
                        "WHERE bc1.biography_id = ?\n" +
                        "LIMIT ?\n" +
                        "OFFSET ?",
                ps -> {
                    ps.setInt(1, biographyId);
                    ps.setInt(2, limit);
                    ps.setLong(3, offset);
                },
                (rs, rowNum) -> {
                    BiographyComment biographyComment = new BiographyComment();

                    biographyComment.setId(rs.getInt("id"));
                    biographyComment.setCreatedAt(rs.getTimestamp("created_at"));
                    biographyComment.setContent(rs.getString("content"));
                    biographyComment.setBiographyId(rs.getInt("biography_id"));
                    biographyComment.setUserName(rs.getString("user_name"));

                    Biography biography = new Biography.Builder()
                            .setFirstName(rs.getString("first_name"))
                            .setLastName(rs.getString("last_name"))
                            .build();

                    biographyComment.setBiography(biography);

                    int parentId = rs.getInt("parent_id");

                    if (!rs.wasNull()) {
                        biographyComment.setParentId(parentId);

                        BiographyComment parent = new BiographyComment();

                        parent.setId(parentId);

                        Biography replyTo = new Biography.Builder()
                                .setFirstName(rs.getString("reply_first_name"))
                                .setUserName(rs.getString("reply_user_name"))
                                .build();

                        parent.setBiography(replyTo);

                        biographyComment.setParent(parent);
                    }

                    return biographyComment;
                }
        );
    }

    public long countOffByBiographyId(int biographyId) {
        return jdbcTemplate.query(
                "SELECT COUNT(*) FROM biography_comment WHERE biography_id=?",
                ps -> ps.setInt(1, biographyId),
                rs -> {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }

                    return 0L;
                }
        );
    }

    public Map<Integer, Long> countOffByBiographiesIds(Collection<Integer> biographiesIds) {
        if (biographiesIds.isEmpty()) {
            Map<Integer, Long> result = new HashMap<>();

            biographiesIds.forEach(integer -> result.put(integer, 0L));

            return result;
        }
        String inClause = biographiesIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT biography_id, COUNT(biography_id) as cnt FROM biography_comment ");
        sql.append("WHERE biography_id IN (");
        sql.append(inClause);
        sql.append(") GROUP BY biography_id");

        return jdbcTemplate.query(
                sql.toString(),
                rs -> {
                    Map<Integer, Long> result = new HashMap<>();

                    while (rs.next()) {
                        result.put(rs.getInt("biography_id"), rs.getLong("cnt"));
                    }

                    biographiesIds
                            .stream()
                            .filter(integer -> !result.containsKey(integer))
                            .forEach(integer -> result.put(integer, 0L));

                    return result;
                }
        );
    }
}
