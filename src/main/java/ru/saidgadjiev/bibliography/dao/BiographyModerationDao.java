package ru.saidgadjiev.bibliography.dao;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliography.data.FilterCriteria;
import ru.saidgadjiev.bibliography.data.UpdateValue;
import ru.saidgadjiev.bibliography.domain.Biography;

import java.sql.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static ru.saidgadjiev.bibliography.data.FilterUtils.toClause;

/**
 * Created by said on 25.11.2018.
 */
@Repository
public class BiographyModerationDao {

    private final JdbcTemplate jdbcTemplate;

    public BiographyModerationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Biography getModeratorInfo(int biographyId) {
        return jdbcTemplate.query(
                "SELECT " + getModeratorInfoSelectList() + " FROM biography b LEFT JOIN biography bm ON b.moderator_id = bm.user_id WHERE b.id = ?",
                ps -> ps.setInt(1, biographyId),
                rs -> {
                    if (rs.next()) {
                        return mapModeratorInfo(rs);
                    }

                    return null;
                }
        );
    }

    public Biography update(List<UpdateValue> updateValues, Collection<FilterCriteria> criteria) throws SQLException {
        String clause = toClause(criteria, null);

        StringBuilder sql = new StringBuilder();

        sql.append("UPDATE biography SET ");

        for (Iterator<UpdateValue> iterator = updateValues.iterator(); iterator.hasNext(); ) {
            sql.append(iterator.next().getName()).append(" = ?");

            if (iterator.hasNext()) {
                sql.append(", ");
            }
        }

        if (StringUtils.isNotBlank(clause)) {
            sql.append(" WHERE ").append(clause);
        }

        sql.append(" RETURNING moderation_status, moderator_id, moderation_info");

        return jdbcTemplate.execute(
                sql.toString(),
                (PreparedStatementCallback<Biography>) ps -> {
                    int i = 0;

                    for (UpdateValue updateValue: updateValues) {
                        if (updateValue.isNeedPreparedSet()) {
                            updateValue.getSetter().set(ps, ++i, updateValue.getValue());
                        }
                    }
                    for (FilterCriteria criterion: criteria) {
                        if (criterion.isNeedPreparedSet()) {
                            criterion.getValueSetter().set(ps, ++i, criterion.getFilterValue());
                        }
                    }

                    ps.execute();

                    try (ResultSet resultSet = ps.getResultSet()) {
                        if (resultSet.next()) {
                            Biography biography = new Biography();

                            biography.setModerationStatus(Biography.ModerationStatus.fromCode(resultSet.getInt("moderation_status")));
                            biography.setModeratorId(resultSet.getInt("moderator_id"));
                            biography.setModerationInfo(resultSet.getString("moderation_info"));

                            return biography;
                        }
                    }

                    return null;
                }
        );
    }

    private String getModeratorInfoSelectList() {
        StringBuilder selectList = new StringBuilder();

        selectList.append("bm.user_id as m_user_id,");
        selectList.append("bm.first_name as m_first_name,");
        selectList.append("bm.last_name as m_last_name,");
        selectList.append("bm.id as m_id");

        return selectList.toString();
    }

    private Biography mapModeratorInfo(ResultSet rs) throws SQLException {
        Biography biography = new Biography();

        Biography moderatorBiography = new Biography();

        moderatorBiography.setFirstName(rs.getString("m_first_name"));
        moderatorBiography.setLastName(rs.getString("m_last_name"));
        moderatorBiography.setUserId(rs.getInt("m_user_id"));
        moderatorBiography.setId(rs.getInt("m_id"));

        biography.setModeratorBiography(moderatorBiography);

        return biography;
    }
}
