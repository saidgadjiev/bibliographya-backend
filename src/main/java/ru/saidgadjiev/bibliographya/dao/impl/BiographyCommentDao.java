package ru.saidgadjiev.bibliographya.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.BiographyComment;
import ru.saidgadjiev.bibliographya.utils.SortUtils;

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

    public int create(BiographyComment biographyComment) {
        StringBuilder insert = new StringBuilder();
        String existsPart = "EXISTS(SELECT 1 FROM biography WHERE id = ? AND disable_comments = FALSE)";

        insert
                .append("INSERT INTO biography_comment(")
                .append("content, biography_id, user_id, parent_id, parent_user_id")
                .append(") SELECT ?, ?, ?, ? ");

        if (biographyComment.getParentId() != null) {
            insert
                    .append(",user_id FROM biography_comment WHERE id = ? AND ")
                    .append(existsPart);
        } else {
            insert
                    .append(",NULL WHERE ").append(existsPart);
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int created = jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(insert.toString(), Statement.RETURN_GENERATED_KEYS);

                    ps.setString(1, biographyComment.getContent());
                    ps.setInt(2, biographyComment.getBiographyId());
                    ps.setInt(3, biographyComment.getUserId());

                    if (biographyComment.getParentId() != null) {
                        ps.setInt(4, biographyComment.getParentId());
                    } else {
                        ps.setNull(4, Types.INTEGER);
                    }

                    if (biographyComment.getParentId() != null) {
                        ps.setInt(5, biographyComment.getParentId());
                        ps.setInt(6, biographyComment.getBiographyId());
                    } else {
                        ps.setInt(5, biographyComment.getBiographyId());
                    }

                    return ps;
                },
                keyHolder
        );

        Map<String, Object> keys = keyHolder.getKeys();

        if (keys != null && !keys.isEmpty()) {
            biographyComment.setId(((Number) keys.get("id")).intValue());
            biographyComment.setCreatedAt((Timestamp) keys.get("created_at"));
        }

        return created;
    }

    public int delete(int commentId) {
        return jdbcTemplate.update(
                "DELETE FROM biography_comment WHERE id = ?",
                ps -> ps.setInt(1, commentId)
        );
    }

    public List<BiographyComment> getComments(TimeZone timeZone, int biographyId, Sort sort, int limit, long offset) {
        String sortClause = SortUtils.toSql(sort, "bcm");

        return jdbcTemplate.query(
                "SELECT " + selectList(timeZone) +
                        " FROM biography_comment bcm LEFT JOIN biography_comment bcmp ON bcm.parent_id = bcmp.id\n" +
                        "  LEFT JOIN biography ba ON bcm.user_id = ba.user_id\n" +
                        "  LEFT JOIN biography br ON bcm.parent_user_id = br.user_id\n" +
                        " WHERE bcm.biography_id = ?\n" +
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

    public long countOff() {
        return jdbcTemplate.query(
                "SELECT COUNT(*) as cnt FROM biography_comment",
                rs -> {
                    if (rs.next()) {
                        return rs.getLong("cnt");
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

    public BiographyComment getById(TimeZone timeZone, int id) {
        return jdbcTemplate.query(
                "SELECT " + selectList(timeZone) + " " +
                        "FROM biography_comment bcm LEFT JOIN biography_comment bcmp ON bcm.parent_id = bcmp.id\n" +
                        "  LEFT JOIN biography ba ON bcm.user_id = ba.user_id\n" +
                        "  LEFT JOIN biography br ON bcm.parent_user_id = br.user_id\n" +
                        "WHERE bcm.id = ?",
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

        biographyComment.setId(rs.getInt("bcm_id"));
        biographyComment.setCreatedAt(rs.getTimestamp("bcm_created_at"));
        biographyComment.setContent(rs.getString("bcm_content"));
        biographyComment.setBiographyId(rs.getInt("bcm_biography_id"));
        biographyComment.setUserId(rs.getInt("bcm_user_id"));

        Biography biography = new Biography();

        biography.setId(rs.getInt("ba_id"));
        biography.setFirstName(rs.getString("ba_first_name"));
        biography.setLastName(rs.getString("ba_last_name"));
        biography.setUserId(rs.getInt("ba_user_id"));

        biographyComment.setUser(biography);

        int parentId = rs.getInt("bcm_parent_id");

        if (!rs.wasNull()) {
            biographyComment.setParentId(parentId);

            rs.getInt("bcmp_id");

            if (rs.wasNull()) {
                biographyComment.setParentDeleted(true);
            }
        }
        int parentUserId = rs.getInt("bcm_parent_user_id");

        if (!rs.wasNull()) {
            biographyComment.setParentUserId(parentUserId);

            Biography replyTo = new Biography();
            replyTo.setId(rs.getInt("br_id"));
            replyTo.setFirstName(rs.getString("br_first_name"));
            replyTo.setUserId(rs.getInt("br_user_id"));

            biographyComment.setParentUser(replyTo);
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

    private String selectList(TimeZone timeZone) {
        StringBuilder builder = new StringBuilder();

        builder
                .append("bcm.id as bcm_id,")
                .append("bcm.content as bcm_content,")
                .append("bcm.created_at::TIMESTAMPTZ AT TIME ZONE '").append(timeZone.getID()).append("' as bcm_created_at,")
                .append("bcm.biography_id as bcm_biography_id,")
                .append("bcm.user_id as bcm_user_id,")
                .append("bcm.parent_id as bcm_parent_id,")
                .append("bcm.parent_user_id as bcm_parent_user_id,")
                .append("bcmp.id as bcmp_id,")
                .append("ba.id as ba_id,")
                .append("ba.first_name as ba_first_name,")
                .append("ba.last_name as ba_last_name,")
                .append("ba.user_id as ba_user_id,")
                .append("br.id as br_id,")
                .append("br.first_name as br_first_name,")
                .append("br.user_id as br_user_id");

        return builder.toString();
    }
}
