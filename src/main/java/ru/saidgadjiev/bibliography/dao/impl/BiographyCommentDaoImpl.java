package ru.saidgadjiev.bibliography.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliography.dao.api.BiographyCommentDao;
import ru.saidgadjiev.bibliography.data.FilterCriteria;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.BiographyComment;
import ru.saidgadjiev.bibliography.utils.FilterUtils;
import ru.saidgadjiev.bibliography.utils.SortUtils;

import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
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

    @Override
    public int delete(int biographyId, int commentId) {
        return jdbcTemplate.update(
                "DELETE FROM biography_comment WHERE id = ?",
                ps -> ps.setInt(1, commentId)
        );
    }

    @Override
    public List<BiographyComment> getComments(int biographyId, Sort sort, int limit, long offset, Integer afterKey) {
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
}
