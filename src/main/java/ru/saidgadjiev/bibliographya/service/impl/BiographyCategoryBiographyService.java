package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.dao.impl.BiographyCategoryBiographyDao;
import ru.saidgadjiev.bibliographya.domain.BiographyCategoryBiography;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    public Map<Integer, BiographyCategoryBiography> getBiographiesCategories(Collection<Integer> biographiesIds) {
        return dao.getBiographiesCategories(biographiesIds);
    }

    public BiographyCategoryBiography getBiographyCategories(Integer biographyId) {
        return dao.getBiographiesCategories(Collections.singletonList(biographyId)).get(biographyId);
    }

    public void addCategoriesToBiography(List<Integer> categoriesIds, Integer biographyId) {
        dao.addCategories(categoriesIds, biographyId);
    }

    public void deleteCategoriesFromBiography(List<Integer> categoriesIds, Integer biographyId) {
        dao.deleteCategories(categoriesIds, biographyId);
    }
}
