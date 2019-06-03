package ru.saidgadjiev.bibliographya.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.saidgadjiev.bibliographya.domain.Profession;
import ru.saidgadjiev.bibliographya.model.BiographyProfession;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class BiographyProfessionDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public BiographyProfessionDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<Integer, BiographyProfession> getBiographiesProfessions(Collection<Integer> biographiesIds) {
        if (biographiesIds.isEmpty()) {
            return Collections.emptyMap();
        }
        String inClause = biographiesIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        return jdbcTemplate.query(
                "SELECT bp.biography_id, p.id, p.name FROM biography_profession bp " +
                        "INNER JOIN profession p ON bp.profession_id = p.id WHERE bp.biography_id IN (" + inClause + ")",
                rs -> {
                    Map<Integer, BiographyProfession> result = new HashMap<>();

                    biographiesIds.forEach(integer -> result.put(integer, new BiographyProfession()));

                    while (rs.next()) {
                        int biographyId = rs.getInt("biography_id");
                        int categoryId = rs.getInt("id");
                        String professionName = rs.getString("name");

                        result.get(biographyId).setBiographyId(biographyId);

                        Profession profession = new Profession();

                        profession.setId(categoryId);
                        profession.setName(professionName);

                        result.get(biographyId).getProfessions().add(profession);
                    }

                    return result;
                }
        );
    }

    public void addProfessions(List<Integer> professionsIds, Integer biographyId) {
        jdbcTemplate.batchUpdate(
                "INSERT INTO " +
                        "biography_profession(profession_id, biography_id) " +
                        "VALUES(?, " + biographyId + ") ON CONFLICT DO NOTHING",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, professionsIds.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return professionsIds.size();
                    }
                }
        );
    }

    public void deleteProfessions(List<Integer> professionsIds, Integer biographyId) {
        jdbcTemplate.batchUpdate(
                "DELETE FROM biography_profession WHERE biography_id = " + biographyId + " AND profession_id = ?",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, professionsIds.get(i));
                    }

                    @Override
                    public int getBatchSize() {
                        return professionsIds.size();
                    }
                }
        );
    }
}
