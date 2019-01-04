package ru.saidgadjiev.bibliography.dao;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.BiographyComment;
import ru.saidgadjiev.bibliography.utils.SortUtils;

import java.sql.*;
import java.util.*;
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

    public BiographyComment create(BiographyComment biographyComment) {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("INSERT INTO biography_comment" +
                    "(content, biography_id, user_id, parent_id) " +
                    "VALUES(?, ?, ?, ?) RETURNING id, created_at")) {
                ps.setString(1, biographyComment.getContent());
                ps.setInt(2, biographyComment.getBiographyId());
                ps.setInt(3, biographyComment.getUserId());

                if (biographyComment.getParentId() != null) {
                    ps.setInt(4, biographyComment.getParentId());
                } else {
                    ps.setNull(4, Types.INTEGER);
                }

                ps.execute();

                try (ResultSet resultSet = ps.getResultSet()) {
                    if (resultSet.next()) {
                        biographyComment.setId(resultSet.getInt("id"));
                        biographyComment.setCreatedAt(resultSet.getTimestamp("created_at"));
                    }
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(ex.getMessage(), ex) {
            };
        }

        return biographyComment;
    }

    public int delete(int commentId) {
        return jdbcTemplate.update(
                "DELETE FROM biography_comment WHERE id = ?",
                ps -> ps.setInt(1, commentId)
        );
    }

    public List<BiographyComment> getComments(int biographyId, Sort sort, int limit, long offset) {
        String sortClause = SortUtils.toSql(sort, "bc1");

        return jdbcTemplate.query(
                "SELECT " +
                        "  bc1.*,\n" +
                        "  bg1.id as biography_id, " +
                        "  bg1.first_name,\n" +
                        "  bg1.last_name,\n" +
                        "  bg2.id as reply_biography_id," +
                        "  bg2.user_id AS reply_user_name,\n" +
                        "  bg2.first_name AS reply_first_name\n" +
                        " FROM biography_comment bc1 LEFT JOIN biography_comment bc2 ON bc1.parent_id = bc2.id\n" +
                        "  LEFT JOIN biography bg1 ON bc1.user_id = bg1.user_id\n" +
                        "  LEFT JOIN biography bg2 ON bc2.user_id = bg2.user_id\n" +
                        " WHERE bc1.biography_id = ?\n" +
                        (StringUtils.isNotBlank(sortClause) ? " ORDER BY " + sortClause : "") +
                        " LIMIT ? " +
                        " OFFSET ?",
                ps -> {
                    ps.setInt(1, biographyId);
                    ps.setInt(2, limit);
                    ps.setLong(3, offset);
                },
                (rs, rowNum) -> map(rs)
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

    public BiographyComment getById(int id) {
        return jdbcTemplate.query(
                "SELECT\n" +
                        "  bc1.*,\n" +
                        "  bg1.id AS biography_id," +
                        "  bg1.first_name,\n" +
                        "  bg1.last_name,\n" +
                        "  bg2.id AS reply_biography_id," +
                        "  bg2.user_id AS reply_user_id,\n" +
                        "  bg2.first_name AS reply_first_name\n" +
                        "FROM biography_comment bc1 LEFT JOIN biography_comment bc2 ON bc1.parent_id = bc2.id\n" +
                        "  LEFT JOIN biography bg1 ON bc1.user_id = bg1.user_id\n" +
                        "  LEFT JOIN biography bg2 ON bc2.user_id = bg2.user_id\n" +
                        "WHERE bc1.id = ?",
                ps -> {
                    ps.setInt(1, id);
                },
                rs -> {
                    if (rs.next()) {
                        return map(rs);
                    }

                    return null;
                }
        );
    }

    private BiographyComment map(ResultSet rs) throws SQLException {
        BiographyComment biographyComment = new BiographyComment();

        biographyComment.setId(rs.getInt("id"));
        biographyComment.setCreatedAt(rs.getTimestamp("created_at"));
        biographyComment.setContent(rs.getString("content"));
        biographyComment.setBiographyId(rs.getInt("biography_id"));
        biographyComment.setUserId(rs.getInt("user_id"));

        Biography biography = new Biography();

        biography.setId(rs.getInt("biography_id"));
        biography.setFirstName(rs.getString("first_name"));
        biography.setLastName(rs.getString("last_name"));

        biographyComment.setBiography(biography);

        int parentId = rs.getInt("parent_id");

        if (!rs.wasNull()) {
            biographyComment.setParentId(parentId);

            BiographyComment parent = new BiographyComment();

            parent.setId(parentId);

            Biography replyTo = new Biography();
            replyTo.setId(rs.getInt("reply_biography_id"));
            replyTo.setFirstName(rs.getString("reply_first_name"));
            replyTo.setUserId(rs.getInt("reply_user_id"));

            parent.setBiography(replyTo);
            parent.setBiographyId(replyTo.getId());

            biographyComment.setParent(parent);
        }

        return biographyComment;
    }

    public int updateContent(Integer commentId, String content) {
        return jdbcTemplate.update(
                "UPDATE biography_comment SET content = ? WHERE id = ?",
                ps -> {
                    ps.setString(1, content);
                    ps.setInt(2, commentId);
                }
        );
    }
}
