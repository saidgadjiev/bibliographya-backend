package ru.saidgadjiev.bibliography.dao;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliography.data.FilterCriteria;
import ru.saidgadjiev.bibliography.data.FilterUtils;
import ru.saidgadjiev.bibliography.data.UpdateValue;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.BiographyFix;
import ru.saidgadjiev.bibliography.utils.ResultSetUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.saidgadjiev.bibliography.data.FilterUtils.toClause;

/**
 * Created by said on 15.12.2018.
 */
@Repository
public class BiographyFixDao {

    private final JdbcTemplate jdbcTemplate;

    public BiographyFixDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<BiographyFix> getFixesList(int limit, long offset, Collection<FilterCriteria> criteria) {
        String clause = FilterUtils.toClause(criteria, "bf");

        return jdbcTemplate.query(
                "SELECT " + selectList() +
                        " FROM biography_fix bf INNER JOIN biography b ON bf.biography_id = b.id\n" +
                        "  INNER JOIN biography cb ON bf.creator_id = cb.user_id\n" +
                        "  LEFT JOIN biography fb ON bf.fixer_id = fb.user_id " + (clause.length() > 0 ? " WHERE " + clause : "") +
                        " LIMIT " + limit + "\n" +
                        " OFFSET " + offset,
                ps -> {
                    int i = 0;

                    for (FilterCriteria criterion
                            : criteria
                            .stream()
                            .filter(FilterCriteria::isNeedPreparedSet)
                            .collect(Collectors.toList())) {
                        criterion.getValueSetter().set(ps, ++i, criterion.getFilterValue());
                    }
                },
                (rs, rowNum) -> map(rs)
        );
    }

    public long countOff() {
        return jdbcTemplate.query(
                "SELECT count(*) AS cnt FROM biography_fix",
                rs -> {
                    if (rs.next()) {
                        return rs.getLong("cnt");
                    }

                    return 0L;
                }
        );
    }

    public BiographyFix getFixerInfo(int fixId) {
        return jdbcTemplate.query(
                "SELECT " + fixerInfoSelectList() + " " +
                        "FROM biography_fix bf " +
                        "LEFT JOIN biography fb ON bf.fixer_id = fb.user_id WHERE bf.id =" + fixId + "",
                rs -> {
                    if (rs.next()) {
                        return mapFixerInfo(rs);
                    }

                    return null;
                }
        );
    }

    public BiographyFix update(List<UpdateValue> updateValues, Collection<FilterCriteria> criteria) {
        String clause = toClause(criteria, null);

        StringBuilder sql = new StringBuilder();

        sql.append("UPDATE biography_fix SET ");

        for (Iterator<UpdateValue> iterator = updateValues.iterator(); iterator.hasNext(); ) {
            sql.append(iterator.next().getName()).append(" = ?");

            if (iterator.hasNext()) {
                sql.append(", ");
            }
        }

        if (StringUtils.isNotBlank(clause)) {
            sql.append(" WHERE ").append(clause);
        }

        sql.append(" RETURNING status, fixer_id");

        return jdbcTemplate.execute(
                sql.toString(),
                (PreparedStatementCallback<BiographyFix>) ps -> {
                    int i = 0;

                    for (UpdateValue updateValue : updateValues) {
                        if (updateValue.isNeedPreparedSet()) {
                            updateValue.getSetter().set(ps, ++i, updateValue.getValue());
                        }
                    }
                    for (FilterCriteria criterion : criteria) {
                        if (criterion.isNeedPreparedSet()) {
                            criterion.getValueSetter().set(ps, ++i, criterion.getFilterValue());
                        }
                    }

                    ps.execute();

                    try (ResultSet resultSet = ps.getResultSet()) {
                        if (resultSet.next()) {
                            BiographyFix biographyFix = new BiographyFix();

                            biographyFix.setStatus(BiographyFix.FixStatus.fromCode(resultSet.getInt("status")));
                            biographyFix.setFixerId(ResultSetUtils.intOrNull(resultSet, "fixer_id"));

                            return biographyFix;
                        }
                    }

                    return null;
                }
        );
    }

    private BiographyFix map(ResultSet rs) throws SQLException {
        BiographyFix fix = new BiographyFix();

        fix.setId(rs.getInt("id"));
        fix.setFixText(rs.getString("fix_text"));
        fix.setBiographyId(rs.getInt("biography_id"));
        fix.setFixerId(ResultSetUtils.intOrNull(rs, "fixer_id"));
        fix.setStatus(BiographyFix.FixStatus.fromCode(rs.getInt("status")));

        Biography biography = new Biography();

        biography.setId(rs.getInt("b_id"));
        biography.setFirstName(rs.getString("b_first_name"));
        biography.setLastName(rs.getString("b_last_name"));
        biography.setMiddleName(rs.getString("b_middle_name"));
        biography.setBiography(rs.getString("b_biography"));

        fix.setBiography(biography);

        Biography creatorBiography = new Biography();

        creatorBiography.setId(rs.getInt("cb_id"));
        creatorBiography.setFirstName(rs.getString("cb_first_name"));
        creatorBiography.setLastName(rs.getString("cb_first_name"));
        creatorBiography.setMiddleName(rs.getString("cb_middle_name"));

        fix.setCreatorBiography(creatorBiography);

        if (fix.getFixerId() != null) {
            fix.setFixerBiography(mapFixerBiography(rs));
        }

        return fix;
    }

    private BiographyFix mapFixerInfo(ResultSet rs) throws SQLException {
        BiographyFix fix = new BiographyFix();

        fix.setFixerId(ResultSetUtils.intOrNull(rs,"fixer_id"));
        fix.setStatus(BiographyFix.FixStatus.fromCode(rs.getInt("status")));


        if (fix.getFixerId() != null) {
            fix.setFixerBiography(mapFixerBiography(rs));
        }

        return fix;
    }

    public Biography mapFixerBiography(ResultSet rs) throws SQLException {
        Biography fixerBiography = new Biography();

        fixerBiography.setId(rs.getInt("fb_id"));
        fixerBiography.setFirstName(rs.getString("fb_first_name"));
        fixerBiography.setLastName(rs.getString("fb_first_name"));
        fixerBiography.setMiddleName(rs.getString("fb_middle_name"));
        fixerBiography.setUserId(rs.getInt("fb_user_id"));

        return fixerBiography;
    }

    public void create(BiographyFix biographyFix) {
        jdbcTemplate.update(
                "INSERT INTO biography_fix(fix_text, biography_id, creator_id) VALUES (?, ?, ?)",
                new PreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps) throws SQLException {
                        ps.setString(1, biographyFix.getFixText());
                        ps.setInt(2, biographyFix.getBiographyId());
                        ps.setInt(3, biographyFix.getCreatorId());
                    }
                }
        );
    }

    private String selectList() {
        StringBuilder builder = new StringBuilder();

        builder
                .append("bf.id,")
                .append("bf.fix_text,")
                .append("bf.biography_id,")
                .append("bf.fixer_id,")
                .append("bf.status,")
                .append("b.id as b_id,")
                .append("b.first_name as b_first_name,")
                .append("b.last_name as b_last_name,")
                .append("b.middle_name as b_middle_name,")
                .append("b.biography as b_biography,")
                .append("cb.id as cb_id,")
                .append("cb.first_name as cb_first_name,")
                .append("cb.last_name as cb_last_name,")
                .append("cb.middle_name as cb_middle_name,")
                .append("fb.id as fb_id,")
                .append("fb.first_name as fb_first_name,")
                .append("fb.user_id as fb_user_id,")
                .append("fb.last_name as fb_last_name,")
                .append("fb.middle_name as fb_middle_name");

        return builder.toString();
    }

    public String fixerInfoSelectList() {
        StringBuilder selectList = new StringBuilder();

        selectList
                .append("bf.fixer_id,")
                .append("bf.status,")
                .append("fb.id as fb_id,")
                .append("fb.first_name as fb_first_name,")
                .append("fb.user_name as fb_user_name,")
                .append("fb.last_name as fb_last_name,")
                .append("fb.middle_name as fb_middle_name");

        return selectList.toString();
    }
}
