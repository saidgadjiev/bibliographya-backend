package ru.saidgadjiev.bibliography.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliography.dao.BiographyCategoryBiographyDao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by said on 01.12.2018.
 */
@Service
public class BiographyCategoryBiographyService {

    private final BiographyCategoryBiographyDao dao;

    @Autowired
    public BiographyCategoryBiographyService(BiographyCategoryBiographyDao dao) {
        this.dao = dao;
    }

    public Map<Integer, Collection<String>> getBiographiesCategories(Collection<Integer> biographiesIds) {
        return dao.getBiographiesCategories(biographiesIds);
    }

    public Collection<String> getBiographyCategories(Integer biographyId) {
        return dao.getBiographiesCategories(Collections.singletonList(biographyId)).get(biographyId);
    }

    public void addCategoriesToBiography(Collection<String> categories, Integer biographyId) {
        dao.addCategories(categories, biographyId);
    }

    public void deleteCategoriesFromBiography(Collection<String> categories, Integer biographyId) {
        dao.deleteCategories(categories, biographyId);
    }
}
