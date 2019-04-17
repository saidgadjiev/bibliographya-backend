package ru.saidgadjiev.bibliographya.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class StashMediaDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public StashMediaDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(String path) {
        jdbcTemplate.update(
                "INSERT INTO media_stash(path) VALUES(?)",
                ps -> ps.setString(1, path)
        );
    }

    public void remove(String path) {
        jdbcTemplate.update(
                "DELETE FROM media_stash WHERE path = ?",
                ps -> ps.setString(1, path)
        );
    }
}
