package ru.saidgadjiev.bibliographya.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliographya.domain.BiographyCategory;
import ru.saidgadjiev.bibliographya.domain.BiographyCategoryBiography;

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

    public Map<Integer, BiographyCategoryBiography> getBiographiesCategories(Collection<Integer> biographiesIds) {
        if (biographiesIds.isEmpty()) {
            return Collections.emptyMap();
        }
        String inClause = biographiesIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        return jdbcTemplate.query(
                "SELECT bb.biography_id, bc.id, bc.name FROM biography_category_biography bb " +
                        "INNER JOIN biography_category bc ON bb.category_id = bc.id WHERE bb.biography_id IN (" + inClause + ")",
                rs -> {
                    Map<Integer, BiographyCategoryBiography> result = new HashMap<>();

                    biographiesIds.forEach(integer -> result.put(integer, new BiographyCategoryBiography()));

                    while (rs.next()) {
                        int biographyId = rs.getInt("biography_id");
                        int categoryId = rs.getInt("id");
                        String categoryName = rs.getString("name");

                        result.get(biographyId).setBiographyId(biographyId);

                        BiographyCategory biographyCategory = new BiographyCategory();

                        biographyCategory.setId(categoryId);
                        biographyCategory.setName(categoryName);

                        result.get(biographyId).getCategories().add(biographyCategory);
                    }

                    return result;
                }
        );
    }

    public void addCategories(Collection<Integer> categories, Integer biographyId) {
        Iterator<Integer> iterator = categories.iterator();

        jdbcTemplate.batchUpdate(
                "INSERT INTO " +
                        "biography_category_biography(category_id, biography_id) " +
                        "VALUES(?, " + biographyId + ") ON CONFLICT DO NOTHING",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, iterator.next());
                    }

                    @Override
                    public int getBatchSize() {
                        return categories.size();
                    }
                }
        );
    }

    public void deleteCategories(Collection<Integer> categories, Integer biographyId) {
        Iterator<Integer> iterator = categories.iterator();

        jdbcTemplate.batchUpdate(
                "DELETE FROM biography_category_biography WHERE biography_id = " + biographyId + " AND category_id = ?",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, iterator.next());
                    }

                    @Override
                    public int getBatchSize() {
                        return categories.size();
                    }
                }
        );
    }
}
