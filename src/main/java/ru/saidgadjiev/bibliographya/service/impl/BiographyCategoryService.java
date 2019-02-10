package ru.saidgadjiev.bibliographya.service.impl;

import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.dao.impl.BiographyCategoryDao;
import ru.saidgadjiev.bibliographya.domain.BiographyCategory;
import ru.saidgadjiev.bibliographya.model.BiographyCategoryRequest;

import java.util.List;

/**
 * Created by said on 27.11.2018.
 */
@Service
public class BiographyCategoryService {

    private final BiographyCategoryDao dao;

    @Autowired
    public BiographyCategoryService(BiographyCategoryDao dao) {
        this.dao = dao;
    }

    public Page<BiographyCategory> getCategories(Pageable pageRequest) {
        List<BiographyCategory> categories = dao.getList(pageRequest.getPageSize(), pageRequest.getOffset());

        return new PageImpl<>(categories, pageRequest, categories.size());
    }

    @Nullable
    public BiographyCategory getById(int id) {
        return dao.getById(id);
    }

    public BiographyCategory create(BiographyCategoryRequest categoryRequest) {
        BiographyCategory category = new BiographyCategory();

        category.setName(categoryRequest.getName());
        category.setImagePath(categoryRequest.getImagePath());

        return dao.create(category);
    }

    public int deleteById(int id) {
        return dao.deleteById(id);
    }

    public int update(int id, BiographyCategoryRequest categoryRequest) {
        BiographyCategory biographyCategory = new BiographyCategory();

        biographyCategory.setId(id);
        biographyCategory.setName(categoryRequest.getName());
        biographyCategory.setImagePath(categoryRequest.getImagePath());

        return dao.update(biographyCategory);
    }

    public int updatePath(int id, String newPath) {
        return dao.updatePath(id, newPath);
    }
}
