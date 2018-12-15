package ru.saidgadjiev.bibliography.dao;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliography.domain.BiographyFix;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by said on 15.12.2018.
 */
@Repository
public class BiographyFixDao {

    private final JdbcTemplate jdbcTemplate;

    public BiographyFixDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<BiographyFix> getFixesList(int limit, long offset) {
        return jdbcTemplate.query(
                "SELECT * FROM biography_fix LIMIT " + limit + " OFFSET " + offset,
                (rs, rowNum) -> map(rs)
        );
    }

    public long countOff() {
        return jdbcTemplate.query(
                "SELECT count(*) as cnt FROM biography_fix",
                rs -> {
                    if (rs.next()) {
                        return rs.getLong("cnt");
                    }

                    return 0L;
                }
        );
    }

    private BiographyFix map(ResultSet rs) throws SQLException {
        BiographyFix fix = new BiographyFix();

        fix.setId(rs.getInt("id"));
        fix.setFixText(rs.getString("fix_text"));
        fix.setBiographyId(rs.getInt("biography_id"));
        fix.setFixerName(rs.getString("fixer_name"));
        fix.setFixStatus(BiographyFix.FixStatus.fromCode(rs.getInt("status")));

        return fix;
    }
}
