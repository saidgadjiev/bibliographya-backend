package ru.saidgadjiev.bibliography.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by said on 01.12.2018.
 */
@Repository
public class BiographyCategoryBiographyDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public BiographyCategoryBiographyDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<Integer, String> getBiographiesCategories(Collection<Integer> biographiesIds) {
        String inClause = biographiesIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        return jdbcTemplate.query(
                "SELECT * FROM biography_category_biography WHERE biography_id IN (" + inClause + ")",
                new ResultSetExtractor<Map<Integer, String>>() {
                    @Override
                    public Map<Integer, String> extractData(ResultSet rs) throws SQLException, DataAccessException {
                        Map<Integer, String> result = new HashMap<>();

                        return null;
                    }
                }
        );
    }
}
