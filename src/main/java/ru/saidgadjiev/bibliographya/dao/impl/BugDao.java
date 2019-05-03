package ru.saidgadjiev.bibliographya.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliographya.dao.dialect.Dialect;
import ru.saidgadjiev.bibliographya.dao.impl.dsl.DslVisitor;
import ru.saidgadjiev.bibliographya.data.PreparedSetter;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.AndCondition;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.Expression;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.Bug;
import ru.saidgadjiev.bibliographya.utils.ResultSetUtils;
import ru.saidgadjiev.bibliographya.utils.SortUtils;

import java.sql.*;
import java.util.*;

@Repository
public class BugDao {

    private final JdbcTemplate jdbcTemplate;

    private Dialect dialect;

    @Autowired
    public BugDao(JdbcTemplate jdbcTemplate, Dialect dialect) {
        this.jdbcTemplate = jdbcTemplate;
        this.dialect = dialect;
    }

    public Dialect getDialect() {
        return dialect;
    }

    public Bug create(TimeZone timeZone, Bug bug) {
        StringBuilder query = new StringBuilder();

        query.append("INSERT INTO bug(theme, bug_case) VALUES (?, ?)");

        if (dialect.supportReturning()) {
            query
                    .append(" RETURNING id, theme, bug_case, status, created_at::TIMESTAMPTZ AT TIME ZONE '")
                    .append(timeZone.getID())
                    .append("' as created_at");

            return jdbcTemplate.execute(
                    query.toString(),
                    (PreparedStatementCallback<Bug>) preparedStatement -> {
                        preparedStatement.setString(1, bug.getTheme());
                        preparedStatement.setString(2, bug.getBugCase());

                        preparedStatement.execute();

                        try (ResultSet resultSet = preparedStatement.getResultSet()) {
                            if (resultSet.next()) {
                                Bug result = new Bug();

                                result.setId(resultSet.getInt("id"));
                                result.setTheme(resultSet.getString("theme"));
                                result.setBugCase(resultSet.getString("bug_case"));
                                result.setStatus(Bug.BugStatus.fromCode(resultSet.getInt("status")));
                                result.setCreatedAt(resultSet.getTimestamp("created_at"));

                                return result;
                            }
                        }

                        return null;
                    }
            );
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                connection -> {
                    PreparedStatement preparedStatement = connection.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS);

                    preparedStatement.setString(1, bug.getTheme());
                    preparedStatement.setString(2, bug.getBugCase());

                    return preparedStatement;
                },
                keyHolder
        );
        if (keyHolder.getKeys() != null) {
            bug.setId(((Number) keyHolder.getKeys().get("id")).intValue());
            bug.setCreatedAt((Timestamp) keyHolder.getKeys().get("created_at"));
            bug.setStatus(Bug.BugStatus.fromCode(((Number) keyHolder.getKeys().get("status")).intValue()));
        }

        return bug;
    }

    public Bug update(Collection<UpdateValue> values, AndCondition criteria, List<PreparedSetter> setValues) {
        DslVisitor visitor = new DslVisitor(null);

        new Expression() {{
            add(criteria);
        }}.accept(visitor);

        String clause = visitor.getClause();

        StringBuilder sql = new StringBuilder();

        sql.append("UPDATE bug SET ");

        for (Iterator<UpdateValue> iterator = values.iterator(); iterator.hasNext(); ) {
            sql.append(iterator.next().getName()).append(" = ?");

            if (iterator.hasNext()) {
                sql.append(", ");
            }
        }

        if (StringUtils.isNotBlank(clause)) {
            sql.append(" WHERE ").append(clause);
        }

        if (dialect.supportReturning()) {
            sql.append(" RETURNING id, status, fixer_id, info");

            return jdbcTemplate.execute(
                    sql.toString(),
                    (PreparedStatementCallback<Bug>) ps -> {
                        int i = 0;

                        for (UpdateValue updateValue : values) {
                            updateValue.getSetter().set(ps, ++i);
                        }
                        for (PreparedSetter preparedSetter: setValues) {
                            preparedSetter.set(ps, ++i);
                        }

                        ps.execute();

                        try (ResultSet resultSet = ps.getResultSet()) {
                            if (resultSet.next()) {
                                Bug bug = new Bug();

                                bug.setId(resultSet.getInt("id"));
                                bug.setStatus(Bug.BugStatus.fromCode(resultSet.getInt("status")));
                                bug.setFixerId(ResultSetUtils.intOrNull(resultSet, "fixer_id"));
                                bug.setInfo(resultSet.getString("info"));

                                return bug;
                            }
                        }

                        return null;
                    }
            );
        }

        int updated = jdbcTemplate.update(
                sql.toString(),
                ps -> {
                    int i = 0;

                    for (UpdateValue updateValue : values) {
                        updateValue.getSetter().set(ps, ++i);
                    }
                    for (PreparedSetter preparedSetter: setValues) {
                        preparedSetter.set(ps, ++i);
                    }
                }
        );

        return updated == 1 ? new Bug() : null;
    }

    public Bug getById(TimeZone timeZone, int id) {
        return jdbcTemplate.query(
                "SELECT " + selectList(timeZone, Collections.emptySet()) + " FROM bug WHERE id = " + id,
                resultSet -> {
                    if (resultSet.next()) {
                        return mapFull(resultSet, Collections.emptySet());
                    }

                    return null;
                }
        );
    }

    public List<Bug> getList(TimeZone timeZone,
                             int limit,
                             long offset,
                             Sort sort,
                             AndCondition andCondition,
                             List<PreparedSetter> values,
                             Set<String> fields) {
        DslVisitor visitor = new DslVisitor("b");

        new Expression() {{
            add(andCondition);
        }}.accept(visitor);

        String clause = visitor.getClause();

        StringBuilder sql = new StringBuilder();

        sql
                .append("SELECT ")
                .append(selectList(timeZone, fields))
                .append(" FROM bug b")
                .append(" LEFT JOIN biography fb ON b.fixer_id = fb.user_id ");

        if (clause.length() > 0) {
            sql.append("WHERE ").append(clause).append(" ");
        }

        String sortClause = SortUtils.toSql(sort, "b");

        if (StringUtils.isNotBlank(sortClause)) {
            sql.append("ORDER BY ").append(sortClause).append(" ");
        }

        sql
                .append("LIMIT ")
                .append(limit)
                .append(" OFFSET ")
                .append(offset);

        return jdbcTemplate.query(
                sql.toString(),
                ps -> {
                    int i = 0;

                    for (PreparedSetter preparedSetter: values) {
                        preparedSetter.set(ps, ++i);
                    }
                },
                (resultSet, i) -> mapFull(resultSet, fields)
        );
    }

    public Bug getFixerInfo(int bugId) {
        return jdbcTemplate.query(
                "SELECT " + fixerInfoSelectList() + " " +
                        "FROM bug b " +
                        "LEFT JOIN biography fb ON b.fixer_id = fb.user_id WHERE b.id =" + bugId + "",
                rs -> {
                    if (rs.next()) {
                        return mapFixerInfo(rs);
                    }

                    return null;
                }
        );
    }

    private Bug mapFixerInfo(ResultSet rs) throws SQLException {
        Bug fix = new Bug();

        fix.setId(rs.getInt("id"));
        fix.setFixerId(ResultSetUtils.intOrNull(rs, "fixer_id"));
        fix.setStatus(Bug.BugStatus.fromCode(rs.getInt("status")));


        if (fix.getFixerId() != null) {
            fix.setFixer(mapFixerBiography(rs));
        }

        return fix;
    }

    private Biography mapFixerBiography(ResultSet rs) throws SQLException {
        Biography fixerBiography = new Biography();

        fixerBiography.setId(rs.getInt("fb_id"));
        fixerBiography.setFirstName(rs.getString("fb_first_name"));
        fixerBiography.setLastName(rs.getString("fb_last_name"));
        fixerBiography.setUserId(rs.getInt("fb_user_id"));

        return fixerBiography;
    }

    private Bug mapFull(ResultSet resultSet, Set<String> fields) throws SQLException {
        Bug result = new Bug();

        result.setId(resultSet.getInt("id"));
        result.setTheme(resultSet.getString("theme"));
        result.setBugCase(resultSet.getString("bug_case"));
        result.setStatus(Bug.BugStatus.fromCode(resultSet.getInt("status")));
        result.setCreatedAt(resultSet.getTimestamp("created_at"));
        result.setFixedAt(resultSet.getTimestamp("fixed_at"));
        result.setInfo(resultSet.getString("info"));
        result.setFixerId(ResultSetUtils.intOrNull(resultSet, "fixer_id"));

        if (fields.contains("fixer") && result.getFixerId() != null) {
            result.setFixer(mapFixerBiography(resultSet));
        }

        return result;
    }

    private String fixerInfoSelectList() {
        StringBuilder selectList = new StringBuilder();

        selectList
                .append("b.id,")
                .append("b.fixer_id,")
                .append("b.status,")
                .append("fb.id as fb_id,")
                .append("fb.first_name as fb_first_name,")
                .append("fb.user_id as fb_user_id,")
                .append("fb.last_name as fb_last_name");

        return selectList.toString();
    }

    private String selectList(TimeZone timeZone, Set<String> fields) {
        StringBuilder selectList = new StringBuilder();

        selectList
                .append("b.id,")
                .append("b.theme,")
                .append("b.bug_case,")
                .append("b.status,")
                .append("b.created_at::TIMESTAMPTZ AT TIME ZONE '").append(timeZone.getID()).append("' as created_at, ")
                .append("b.status,")
                .append("b.fixed_at::TIMESTAMPTZ AT TIME ZONE '").append(timeZone.getID()).append("' fixed_at, ")
                .append("b.info,")
                .append("b.fixer_id");

        if (fields.contains("fixer")) {
            selectList
                    .append(",fb.id as fb_id,")
                    .append("fb.first_name as fb_first_name,")
                    .append("fb.user_id as fb_user_id,")
                    .append("fb.last_name as fb_last_name");
        }

        return selectList.toString();
    }
}
