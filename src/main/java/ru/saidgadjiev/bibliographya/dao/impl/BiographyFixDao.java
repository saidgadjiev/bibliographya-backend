package ru.saidgadjiev.bibliographya.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliographya.dao.impl.dsl.DslVisitor;
import ru.saidgadjiev.bibliographya.data.PreparedSetter;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.AndCondition;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.Expression;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.BiographyFix;
import ru.saidgadjiev.bibliographya.domain.Country;
import ru.saidgadjiev.bibliographya.utils.ResultSetUtils;
import ru.saidgadjiev.bibliographya.utils.SortUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by said on 15.12.2018.
 */
@Repository
public class BiographyFixDao {

    private final JdbcTemplate jdbcTemplate;

    public BiographyFixDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<BiographyFix> getFixesList(TimeZone timeZone,
                                           int limit,
                                           long offset,
                                           AndCondition criteria,
                                           AndCondition isLikedCriteria,
                                           List<PreparedSetter> values,
                                           Sort sort) {
        DslVisitor visitor = new DslVisitor("bf");

        new Expression() {{
            add(criteria);
        }}.accept(visitor);

        String clause = visitor.getClause();
        String sortClause = SortUtils.toSql(sort, "b");

        StringBuilder sql = new StringBuilder();

        sql
                .append("SELECT ").append(selectList(timeZone)).append(" FROM biography_fix bf INNER JOIN biography b ON bf.biography_id = b.id ")
                .append(" INNER JOIN biography cb ON bf.creator_id = cb.user_id ")
                .append(" LEFT JOIN biography fb ON bf.fixer_id = fb.user_id ")
                .append(" LEFT JOIN biography cbb ON cbb.id = b.creator_id ")
                .append(" LEFT JOIN (SELECT biography_id, COUNT(id) AS cnt FROM biography_like GROUP BY biography_id) l ON bf.biography_id = l.biography_id ")
                .append(" LEFT JOIN (SELECT biography_id, COUNT(id) AS cnt FROM biography_comment GROUP BY biography_id) bc ON bf.biography_id = bc.biography_id ")
                .append(" LEFT JOIN country c ON b.country_id = c.id ");

        visitor = new DslVisitor(null);

        new Expression() {{
            add(isLikedCriteria);
        }}.accept(visitor);

        String isLikedClause = visitor.getClause();

        sql
                .append(" LEFT JOIN (SELECT biography_id FROM biography_like ");

        if (StringUtils.isNotBlank(isLikedClause)) {
            sql.append("WHERE ").append(isLikedClause);
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

                    for (PreparedSetter preparedSetter: values) {
                        preparedSetter.set(ps, ++i);
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

    public BiographyFix update(List<UpdateValue> updateValues, AndCondition andCondition, List<PreparedSetter> values) {
        DslVisitor visitor = new DslVisitor(null);

        new Expression() {{
            add(andCondition);
        }}.accept(visitor);

        String clause = visitor.getClause();

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
                        updateValue.getSetter().set(ps, ++i);
                    }
                    for (PreparedSetter preparedSetter: values) {
                        preparedSetter.set(ps, ++i);
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
        biography.setBio(rs.getString("b_bio"));

        biography.setCountryId(ResultSetUtils.intOrNull(rs, Biography.COUNTRY_ID));

        if (biography.getCountryId() != null) {
            Country country = new Country();

            country.setId(biography.getCountryId());
            country.setName(rs.getString("country"));

            biography.setCountry(country);
        }

        fix.setBiography(biography);

        Biography creatorBiography = new Biography();

        creatorBiography.setId(rs.getInt("cb_id"));
        creatorBiography.setFirstName(rs.getString("cb_first_name"));
        creatorBiography.setLastName(rs.getString("cb_last_name"));
        creatorBiography.setMiddleName(rs.getString("cb_middle_name"));
        creatorBiography.setUserId(rs.getInt("cb_user_id"));

        fix.setCreator(creatorBiography);

        if (fix.getFixerId() != null) {
            fix.setFixer(mapFixerBiography(rs));
        }

        rs.getInt("bisl_biography_id");

        biography.setLiked(!rs.wasNull());

        biography.setLikesCount(rs.getInt("l_cnt"));
        biography.setCommentsCount(rs.getInt("bc_cnt"));

        Biography biographyCreator = new Biography();

        biographyCreator.setId(rs.getInt("cbb_id"));
        biographyCreator.setFirstName(rs.getString("cbb_first_name"));
        biographyCreator.setLastName(rs.getString("cbb_last_name"));
        biographyCreator.setUserId(rs.getInt("cbb_user_id"));

        biography.setCreatorId(biographyCreator.getId());
        biography.setCreator(biographyCreator);
        biography.setCreatedAt(rs.getTimestamp("b_created_at"));
        biography.setUpdatedAt(rs.getTimestamp("b_updated_at"));

        biography.setModerationStatus(Biography.ModerationStatus.fromCode(rs.getInt("b_moderation_status")));

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

    private String selectList(TimeZone timeZone) {
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
                .append("b.creator_id as b_creator_id,")
                .append("b.created_at::TIMESTAMPTZ AT TIME ZONE '").append(timeZone.getID()).append("' as b_created_at, ")
                .append("b.updated_at::TIMESTAMPTZ AT TIME ZONE '").append(timeZone.getID()).append("' as b_updated_at, ")
                .append("b.moderation_status as b_moderation_status,")
                .append("b.first_name as b_first_name,")
                .append("b.last_name as b_last_name,")
                .append("b.middle_name as b_middle_name,")
                .append("c.name as country,")
                .append("b.country_id,")
                .append("b.").append(Biography.BIO).append(" as b_bio,")
                .append("cb.id as cb_id,")
                .append("cb.first_name as cb_first_name,")
                .append("cb.last_name as cb_last_name,")
                .append("cb.middle_name as cb_middle_name,")
                .append("cb.user_id as cb_user_id,")
                .append("fb.id as fb_id,")
                .append("fb.first_name as fb_first_name,")
                .append("fb.user_id as fb_user_id,")
                .append("fb.last_name as fb_last_name,")
                .append("fb.middle_name as fb_middle_name,")
                .append("l.cnt as l_cnt,")
                .append("bc.cnt as bc_cnt,")
                .append("bisl.biography_id as bisl_biography_id,")
                .append("cbb.id as cbb_id,")
                .append("cbb.first_name as cbb_first_name,")
                .append("cbb.last_name as cbb_last_name,")
                .append("cbb.user_id as cbb_user_id");

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
