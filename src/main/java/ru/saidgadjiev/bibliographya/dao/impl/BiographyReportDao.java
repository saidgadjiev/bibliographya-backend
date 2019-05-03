package ru.saidgadjiev.bibliographya.dao.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.BiographyReport;
import ru.saidgadjiev.bibliographya.utils.ResultSetUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by said on 31.12.2018.
 */
@Repository
public class BiographyReportDao {

    private final JdbcTemplate jdbcTemplate;

    public BiographyReportDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Biography> getReports(int limit, long offset) {
        /*return jdbcTemplate.query(
                "SELECT\n" +
                        "  b.*,\n" +
                        "  COUNT(*) AS reportsCount\n" +
                        "FROM biography_report br INNER JOIN biography b ON br.biography_id = b.id\n" +
                        "GROUP BY b.id LIMIT ? OFFSET ?",
                ps -> {
                    ps.setInt(1, limit);
                    ps.setLong(2, offset);
                },
                (rs, rowNum) -> mapBiography(rs)
                );*/

        return Collections.emptyList();
    }

    public Map<Integer, Collection<BiographyReport>> getBiographyReports(Collection<Integer> biographiesIds) {
        if (biographiesIds.isEmpty()) {
            return Collections.emptyMap();
        }
        StringBuilder clause = new StringBuilder();

        String inClause = biographiesIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        clause.append("biography_id IN(").append(inClause).append(")");

        return jdbcTemplate.query(
                "SELECT id, reporter_id, biography_id, status, reason, reason_text FROM biography_report WHERE " + clause.toString(),
                rs -> {
                    Map<Integer, Collection<BiographyReport>> result = new HashMap<>();

                    while (rs.next()) {
                        Integer biographyId = rs.getInt("biography_id");

                        result.putIfAbsent(biographyId, new ArrayList<>());

                        result.get(biographyId).add(map(rs));
                    }

                    return result;
                }
        );
    }

    public long countOff(Object o) {
        /*String clause = FilterUtils.toClause(criteria, null);
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM biography_report ");

        if (StringUtils.isNotBlank(clause)) {
            sql.append("WHERE ").append(clause).append(" ");
        }

        sql.append("GROUP BY biography_id");

        return jdbcTemplate.query(
                sql.toString(),
                ps -> {
                    int i = 0;

                    for (FilterCriteria criterion : criteria) {
                        if (criterion.isNeedPreparedSet()) {
                            criterion.getValueSetter().set(ps, ++i, criterion.getFilterValue());
                        }
                    }
                },
                rs -> {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }

                    return null;
                }
        );*/

        return 0L;
    }

    public int update(Collection<UpdateValue> updateValues, Object o) {
        return jdbcTemplate.update(
                "UPDATE biography_report SET status = ? WHERE id = ?",
                ps -> {
                    ps.setInt(1, 0);
                    ps.setInt(2, 0);
                }
        );
    }

    public BiographyReport create(BiographyReport report) {
        return jdbcTemplate.execute(
                "INSERT INTO biography_report(reporter_id, biography_id, reason, reason_text) VALUES (?, ?, ?, ?) RETURNING *",
                (PreparedStatementCallback<BiographyReport>) ps -> {
                    ps.setInt(1, report.getReporterId());
                    ps.setInt(2, report.getBiographyId());
                    ps.setInt(3, report.getReason().getCode());
                    ps.setString(4, report.getReasonText());

                    ps.execute();

                    try (ResultSet rs = ps.getResultSet()) {
                        if (rs.next()) {
                            return map(rs);
                        }
                    }

                    return null;
                }
        );
    }

    private BiographyReport map(ResultSet rs) throws SQLException {
        BiographyReport report = new BiographyReport();

        report.setId(rs.getInt("id"));
        report.setReporterId(rs.getInt("reporter_id"));
        report.setBiographyId(rs.getInt("biography_id"));
        report.setStatus(BiographyReport.ReportStatus.fromCode(rs.getInt("status")));
        report.setReason(BiographyReport.ReportReason.fromCode(rs.getInt("reason")));
        report.setReasonText(rs.getString("reason_text"));

        return report;
    }

    private Biography mapBiography(ResultSet rs) throws SQLException {
        Biography biography = new Biography();

        biography.setId(rs.getInt("id"));
        biography.setFirstName(rs.getString("first_name"));
        biography.setLastName(rs.getString("last_name"));
        biography.setMiddleName(rs.getString("middle_name"));

        biography.setCreatorId(ResultSetUtils.intOrNull(rs, "creator_id"));
        biography.setUserId(ResultSetUtils.intOrNull(rs, "user_id"));

        biography.setBio(rs.getString("bio"));
        biography.setNewComplaintsCount(rs.getInt("reportsCount"));

        return biography;
    }
}
