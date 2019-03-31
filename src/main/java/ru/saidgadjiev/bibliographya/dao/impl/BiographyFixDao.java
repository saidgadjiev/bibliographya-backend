package ru.saidgadjiev.bibliographya.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.BiographyFix;
import ru.saidgadjiev.bibliographya.utils.ResultSetUtils;
import ru.saidgadjiev.bibliographya.utils.SortUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.saidgadjiev.bibliographya.utils.FilterUtils.toClause;

/**
 * Created by said on 15.12.2018.
 */
@Repository
public class BiographyFixDao {

    private final JdbcTemplate jdbcTemplate;

    public BiographyFixDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<BiographyFix> getFixesList(int limit,
                                           long offset,
                                           Collection<FilterCriteria> criteria,
                                           Collection<FilterCriteria> isLikedCriteria,
                                           Sort sort) {
        String clause = toClause(criteria, "bf");
        String sortClause = SortUtils.toSql(sort, "b");

        StringBuilder sql = new StringBuilder();

        sql
                .append("SELECT ").append(selectList()).append(" FROM biography_fix bf INNER JOIN biography b ON bf.biography_id = b.id ")
                .append(" INNER JOIN biography cb ON bf.creator_id = cb.user_id ")
                .append(" LEFT JOIN biography fb ON bf.fixer_id = fb.user_id ")
                .append(" LEFT JOIN biography bm ON b.moderator_id = bm.user_id ")
                .append(" LEFT JOIN (SELECT biography_id, COUNT(id) AS cnt FROM biography_like GROUP BY biography_id) l ON bf.biography_id = l.biography_id ")
                .append(" LEFT JOIN (SELECT biography_id, COUNT(id) AS cnt FROM biography_comment GROUP BY biography_id) bc ON bf.biography_id = bc.biography_id ");

        String isLikedClause = toClause(isLikedCriteria, "bisl");

        sql
                .append(" LEFT JOIN (SELECT biography_id FROM biography_like ");

        if (StringUtils.isNotBlank(isLikedClause)) {
            sql.append(isLikedClause);
        }

        sql.append(") bisl ON b.id = bisl.biography_id ");

        if (StringUtils.isNotBlank(clause)) {
            sql.append("WHERE ").append(clause).append(" ");
        }
        if (StringUtils.isNotBlank(sortClause)) {
            sql.append("ORDER BY ").append(sortClause).append(" ");
        }
        sql.append("LIMIT ").append(limit).append(" OFFSET ").append(offset);

        return jdbcTemplate.query(
                sql.toString(),
                ps -> {
                    int i = 0;

                    for (FilterCriteria criterion
                            : isLikedCriteria
                            .stream()
                            .filter(FilterCriteria::isNeedPreparedSet)
                            .collect(Collectors.toList())) {
                        criterion.getValueSetter().set(ps, ++i, criterion.getFilterValue());
                    }

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

        sql.append(" RETURNING status, fixer_id, info");

        return jdbcTemplate.execute(
                sql.toString(),
                (PreparedStatementCallback<BiographyFix>) ps -> {
                    int i = 0;

                    for (UpdateValue updateValue : updateValues) {
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
                            BiographyFix biographyFix = new BiographyFix();

                            biographyFix.setStatus(BiographyFix.FixStatus.fromCode(resultSet.getInt("status")));
                            biographyFix.setFixerId(ResultSetUtils.intOrNull(resultSet, "fixer_id"));
                            biographyFix.setInfo(resultSet.getString("info"));

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
        fix.setInfo(rs.getString("info"));
        fix.setCreatorId(rs.getInt("creator_id"));
        fix.setFixText(rs.getString("fix_text"));
        fix.setBiographyId(rs.getInt("biography_id"));
        fix.setFixerId(ResultSetUtils.intOrNull(rs, "fixer_id"));
        fix.setStatus(BiographyFix.FixStatus.fromCode(rs.getInt("status")));

        Biography biography = new Biography();

        biography.setId(rs.getInt("b_id"));
        biography.setFirstName(rs.getString("b_first_name"));
        biography.setLastName(rs.getString("b_last_name"));
        biography.setMiddleName(rs.getString("b_middle_name"));
        biography.setBiography(rs.getString("b_bio"));

        fix.setBiography(biography);

        Biography creatorBiography = new Biography();

        creatorBiography.setId(rs.getInt("cb_id"));
        creatorBiography.setFirstName(rs.getString("cb_first_name"));
        creatorBiography.setLastName(rs.getString("cb_last_name"));
        creatorBiography.setMiddleName(rs.getString("cb_middle_name"));

        fix.setCreator(creatorBiography);

        if (fix.getFixerId() != null) {
            fix.setFixer(mapFixerBiography(rs));
        }

        rs.getInt("bisl_biography_id");

        biography.setLiked(!rs.wasNull());

        biography.setLikesCount(rs.getInt("l_cnt"));
        biography.setCommentsCount(rs.getInt("bc_cnt"));

        return fix;
    }

    private BiographyFix mapFixerInfo(ResultSet rs) throws SQLException {
        BiographyFix fix = new BiographyFix();

        fix.setId(rs.getInt("id"));
        fix.setFixerId(ResultSetUtils.intOrNull(rs, "fixer_id"));
        fix.setStatus(BiographyFix.FixStatus.fromCode(rs.getInt("status")));


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
                .append("bf.creator_id,")
                .append("bf.info,")
                .append("b.id as b_id,")
                .append("b.first_name as b_first_name,")
                .append("b.last_name as b_last_name,")
                .append("b.middle_name as b_middle_name,")
                .append("b.").append(Biography.BIO).append(" as b_bio,")
                .append("cb.id as cb_id,")
                .append("cb.first_name as cb_first_name,")
                .append("cb.last_name as cb_last_name,")
                .append("cb.middle_name as cb_middle_name,")
                .append("fb.id as fb_id,")
                .append("fb.first_name as fb_first_name,")
                .append("fb.user_id as fb_user_id,")
                .append("fb.last_name as fb_last_name,")
                .append("fb.middle_name as fb_middle_name,")
                .append("l.cnt as l_cnt,")
                .append("bc.cnt as bc_cnt,")
                .append(",bisl.biography_id as bisl_biography_id");

        return builder.toString();
    }

    private String fixerInfoSelectList() {
        StringBuilder selectList = new StringBuilder();

        selectList
                .append("bf.id,")
                .append("bf.fixer_id,")
                .append("bf.status,")
                .append("fb.id as fb_id,")
                .append("fb.first_name as fb_first_name,")
                .append("fb.user_id as fb_user_id,")
                .append("fb.last_name as fb_last_name,")
                .append("fb.middle_name as fb_middle_name");

        return selectList.toString();
    }
}
