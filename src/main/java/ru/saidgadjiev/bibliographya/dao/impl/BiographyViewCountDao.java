package ru.saidgadjiev.bibliographya.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BiographyViewCountDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public BiographyViewCountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createOrUpdate(int biographyId, long count) {
        jdbcTemplate.update(
                "INSERT INTO biography_view_count(biography_id, views_count) VALUES(?, ?) ON CONFLICT (biography_id) " +
                        "DO UPDATE SET views_count = biography_view_count.views_count + EXCLUDED.views_count",
                ps -> {
                    ps.setInt(1, biographyId);
                    ps.setLong(2, count);
                }
        );
    }
}
