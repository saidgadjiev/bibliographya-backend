package ru.saidgadjiev.bibliographya.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliographya.dao.api.BiographyCommentDao;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.BiographyComment;
import ru.saidgadjiev.bibliographya.utils.FilterUtils;
import ru.saidgadjiev.bibliographya.utils.SortUtils;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by said on 17.11.2018.
 */
@Repository
@Qualifier("sql")
public class BiographyCommentDaoImpl implements BiographyCommentDao {

    private final JdbcTemplate jdbcTemplate;

    public BiographyCommentDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public BiographyComment create(BiographyComment biographyComment) {
        return jdbcTemplate.execute(
                "INSERT INTO biography_comment" +
                        "(content, biography_id, user_id, parent_id) " +
                        "VALUES(?, ?, ?, ?) RETURNING id, created_at",
                (PreparedStatementCallback<BiographyComment>) ps -> {
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
                    
                    return biographyComment;
                }
        );
    }

    @Override
    public int delete(int biographyId, int commentId) {
        return jdbcTemplate.update(
                "DELETE FROM biography_comment WHERE id = ?",
                ps -> ps.setInt(1, commentId)
        );
    }

    @Override
    public List<BiographyComment> getComments(int biographyId, Sort sort, int limit, long offset) {
        String sortClause = SortUtils.toSql(sort, "bcm");

        return jdbcTemplate.query(
                "SELECT " + selectList() +
                        " FROM biography_comment bcm LEFT JOIN biography_comment bcpm ON bcm.parent_id = bcpm.id\n" +
                        "  LEFT JOIN biography ba ON bcm.user_id = ba.user_id\n" +
                        "  LEFT JOIN biography br ON bcpm.user_id = br.user_id\n" +
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

    @Override
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

    @Override
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

    @Override
    public BiographyComment getById(int id) {
        return jdbcTemplate.query(
                "SELECT " + selectList() + " " +
                        "FROM biography_comment bcm LEFT JOIN biography_comment bcpm ON bcm.parent_id = bcpm.id\n" +
                        "  LEFT JOIN biography ba ON bcm.user_id = ba.user_id\n" +
                        "  LEFT JOIN biography br ON bcpm.user_id = br.user_id\n" +
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

    @Override
    public List<Map<String, Object>> getFields(Collection<String> fields, Collection<FilterCriteria> criteria) {
        StringBuilder builder = new StringBuilder();

        builder.append("SELECT ");

        if (fields.isEmpty()) {
            builder.append("*");
        } else {
            for (Iterator<String> fieldIterator = fields.iterator(); fieldIterator.hasNext(); ) {
                builder.append(fieldIterator.next());

                if (fieldIterator.hasNext()) {
                    builder.append(",");
                }
            }
        }
        builder.append(" ").append("FROM biography_comment ");

        String clause = FilterUtils.toClause(criteria, null);

        if (StringUtils.isNotBlank(clause)) {
            builder.append("WHERE ").append(clause);
        }

        return jdbcTemplate.query(
                builder.toString(),
                ps -> {
                    int i = 0;

                    for (FilterCriteria criterion : criteria) {
                        if (criterion.isNeedPreparedSet()) {
                            criterion.getValueSetter().set(ps, ++i, criterion.getFilterValue());
                        }
                    }
                },
                rs -> {
                    List<Map<String, Object>> result = new ArrayList<>();
                    ResultSetMetaData md = rs.getMetaData();
                    int columns = md.getColumnCount();

                    while (rs.next()){
                        Map<String, Object> row = new HashMap<>(columns);

                        for(int i=1; i<=columns; ++i){
                            row.put(md.getColumnName(i),rs.getObject(i));
                        }
                        result.add(row);
                    }

                    return result;
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

        biographyComment.setUser(biography);

        int parentId = rs.getInt("bcm_parent_id");

        if (!rs.wasNull()) {
            biographyComment.setParentId(parentId);

            BiographyComment parent = new BiographyComment();

            parent.setId(parentId);

            Biography replyTo = new Biography();
            replyTo.setId(rs.getInt("br_id"));
            replyTo.setFirstName(rs.getString("br_first_name"));

            parent.setUser(replyTo);
            parent.setBiographyId(replyTo.getId());

            biographyComment.setParent(parent);
        }

        return biographyComment;
    }

    @Override
    public int updateContent(Integer commentId, String content) {
        return jdbcTemplate.update(
                "UPDATE biography_comment SET content = ? WHERE id = ?",
                ps -> {
                    ps.setString(1, content);
                    ps.setInt(2, commentId);
                }
        );
    }

    private String selectList() {
        StringBuilder builder = new StringBuilder();

        builder
                .append("bcm.id as bcm_id,")
                .append("bcm.content as bcm_content,")
                .append("bcm.created_at as bcm_created_at,")
                .append("bcm.biography_id as bcm_biography_id,")
                .append("bcm.user_id as bcm_user_id,")
                .append("bcm.parent_id as bcm_parent_id,")
                .append("ba.id as ba_id,")
                .append("ba.first_name as ba_first_name,")
                .append("ba.last_name as ba_last_name,")
                .append("br.id as br_id,")
                .append("br.first_name as br_first_name");

        return builder.toString();
    }
}
