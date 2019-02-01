package ru.saidgadjiev.bibliographya.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.BiographyFix;
import ru.saidgadjiev.bibliographya.domain.Bug;
import ru.saidgadjiev.bibliographya.utils.ResultSetUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import static ru.saidgadjiev.bibliographya.utils.FilterUtils.toClause;

@Repository
public class BugDao {

    private final JdbcTemplate jdbcTemplate;

    public BugDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Bug create(Bug bug) {
        return jdbcTemplate.execute(
                "INSERT INTO bug(theme, bug_case) VALUES (?, ?) RETURNING *",
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

                            return result;
                        }
                    }

                    return null;
                }
        );
    }

    public Bug update(Collection<UpdateValue> values, Collection<FilterCriteria> criteria) {
        String clause = toClause(criteria, null);

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

        sql.append(" RETURNING status, fixer_id, info");

        return jdbcTemplate.execute(
                sql.toString(),
                (PreparedStatementCallback<Bug>) ps -> {
                    int i = 0;

                    for (UpdateValue updateValue : values) {
                        updateValue.getSetter().set(ps, ++i, updateValue.getValue());
                    }
                    for (FilterCriteria criterion : criteria) {
                        if (criterion.isNeedPreparedSet()) {
                            criterion.getValueSetter().set(ps, ++i, criterion.getFilterValue());
                        }
                    }

                    ps.execute();

                    try (ResultSet resultSet = ps.getResultSet()) {
                        if (resultSet.next()) {
                            Bug bug = new Bug();

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

        fix.setFixerId(ResultSetUtils.intOrNull(rs,"fixer_id"));
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
        fixerBiography.setMiddleName(rs.getString("fb_middle_name"));
        fixerBiography.setUserId(rs.getInt("fb_user_id"));

        return fixerBiography;
    }

    private String fixerInfoSelectList() {
        StringBuilder selectList = new StringBuilder();

        selectList
                .append("b.fixer_id,")
                .append("b.status,")
                .append("fb.id as fb_id,")
                .append("fb.first_name as fb_first_name,")
                .append("fb.user_id as fb_user_id,")
                .append("fb.last_name as fb_last_name,")
                .append("fb.middle_name as fb_middle_name");

        return selectList.toString();
    }
}
