package ru.saidgadjiev.bibliographya.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliographya.domain.BiographyCategory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by said on 27.11.2018.
 */
@Repository
public class BiographyCategoryDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public BiographyCategoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<BiographyCategory> getList(int limit, long offset) {
        return jdbcTemplate.query(
                "SELECT * FROM biography_category LIMIT " + limit + " OFFSET " + offset,
                (rs, rowNum) -> map(rs)
        );
    }

    public BiographyCategory getByName(String categoryName) {
        return jdbcTemplate.query(
                "SELECT * FROM biography_category WHERE name=?",
                ps -> ps.setString(1, categoryName),
                rs -> {
                    if (rs.next()) {
                        return map(rs);
                    }

                    return null;
                }
        );
    }

    public long countOff() {
        return jdbcTemplate.query(
                "SELECT COUNT(*) FROM biography_category",
                rs -> {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }

                    return 0L;
                }
        );
    }

    public void create(BiographyCategory category) {

    }

    private BiographyCategory map(ResultSet rs) throws SQLException {
        BiographyCategory biographyCategory = new BiographyCategory();

        biographyCategory.setId(rs.getInt("id"));
        biographyCategory.setName(rs.getString("name"));
        biographyCategory.setImagePath(rs.getString("image_path"));

        return biographyCategory;
    }
}
