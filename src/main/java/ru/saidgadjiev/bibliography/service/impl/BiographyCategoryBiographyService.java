package ru.saidgadjiev.bibliography.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliography.dao.impl.BiographyCategoryBiographyDao;

import java.util.*;

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
