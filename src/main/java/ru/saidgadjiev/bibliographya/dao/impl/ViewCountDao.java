package ru.saidgadjiev.bibliographya.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ViewCountDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public ViewCountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createOrUpdate(int biographyId, long count) {
        jdbcTemplate.update(
                "INSERT INTO view_count(biography_id, count) VALUES(?, ?) ON CONFLICT (biography_id) " +
                        "DO UPDATE SET count = count + ?",
                ps -> {
                    ps.setInt(1, biographyId);
                    ps.setLong(2, count);
                    ps.setLong(3, count);
                }
        );
    }
}
