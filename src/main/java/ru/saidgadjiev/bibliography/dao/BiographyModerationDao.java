package ru.saidgadjiev.bibliography.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliography.domain.Biography;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

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
                "SELECT " + getModeratorInfoSelectList() + " FROM biography b LEFT JOIN biography bm ON b.moderator_name = bm.user_name WHERE b.id = ?",
                ps -> ps.setInt(1, biographyId),
                rs -> {
                    if (rs.next()) {
                        return mapModeratorInfo(rs);
                    }

                    return null;
                }
        );
    }

    public int assignMe(int biographyId, String moderatorName) {
        return jdbcTemplate.update(
                "UPDATE biography SET moderator_name = ? WHERE id = ? AND moderator_name IS NULL",
                ps -> {
                    ps.setString(1, moderatorName);
                    ps.setInt(2, biographyId);
                }
        );
    }

    public int release(int biographyId, String moderatorName) {
        return jdbcTemplate.update(
                "UPDATE biography SET moderator_name = NULL, moderation_status = 0 WHERE id = ? AND moderator_name = ?",
                new PreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps) throws SQLException {
                        ps.setInt(1, biographyId);
                        ps.setString(2, moderatorName);
                    }
                }
        );
    }

    public int updateStatus(int biographyId, String moderatorName, int status) {
        return jdbcTemplate.update(
                "UPDATE biography SET moderation_status = ? WHERE id = ? AND moderator_name = ?",
                new PreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps) throws SQLException {
                        ps.setInt(1, status);
                        ps.setInt(2, biographyId);
                        ps.setString(3, moderatorName);
                    }
                }
        );
    }

    private String getModeratorInfoSelectList() {
        StringBuilder selectList = new StringBuilder();

        selectList.append("bm.user_name as m_user_name,");
        selectList.append("bm.first_name as m_first_name,");
        selectList.append("bm.last_name as m_last_name,");
        selectList.append("bm.id as m_id");

        return selectList.toString();
    }

    private Biography mapModeratorInfo(ResultSet rs) throws SQLException {
        Biography biography = new Biography();

        biography.setFirstName(rs.getString("m_first_name"));
        biography.setLastName(rs.getString("m_last_name"));
        biography.setUserName(rs.getString("m_user_name"));
        biography.setId(rs.getInt("m_id"));

        return biography;
    }
}
