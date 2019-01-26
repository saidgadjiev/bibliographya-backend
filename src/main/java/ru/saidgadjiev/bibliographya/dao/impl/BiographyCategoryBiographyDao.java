package ru.saidgadjiev.bibliographya.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
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

    public Map<Integer, Collection<String>> getBiographiesCategories(Collection<Integer> biographiesIds) {
        if (biographiesIds.isEmpty()) {
            return Collections.emptyMap();
        }
        String inClause = biographiesIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        return jdbcTemplate.query(
                "SELECT * FROM biography_category_biography WHERE biography_id IN (" + inClause + ")",
                rs -> {
                    Map<Integer, Collection<String>> result = new HashMap<>();

                    biographiesIds.forEach(integer -> result.put(integer, new ArrayList<>()));

                    while (rs.next()) {
                        result.get(rs.getInt("biography_id")).add(rs.getString("category_name"));
                    }

                    return result;
                }
        );
    }

    public void addCategories(Collection<String> categories, Integer biographyId) {
        Iterator<String> iterator = categories.iterator();

        jdbcTemplate.batchUpdate(
                "INSERT INTO " +
                        "biography_category_biography(category_name, biography_id) " +
                        "VALUES(?, " + biographyId + ") ON CONFLICT DO NOTHING",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, iterator.next());
                    }

                    @Override
                    public int getBatchSize() {
                        return categories.size();
                    }
                }
        );
    }

    public void deleteCategories(Collection<String> categories, Integer biographyId) {
        Iterator<String> iterator = categories.iterator();

        jdbcTemplate.batchUpdate(
                "DELETE FROM biography_category_biography WHERE biography_id = " + biographyId + " AND category_name = ?",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, iterator.next());
                    }

                    @Override
                    public int getBatchSize() {
                        return categories.size();
                    }
                }
        );
    }
}
