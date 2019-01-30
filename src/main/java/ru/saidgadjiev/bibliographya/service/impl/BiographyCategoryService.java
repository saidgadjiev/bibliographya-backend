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

        return new PageImpl<>(categories, pageRequest, dao.countOff());
    }

    @Nullable
    public BiographyCategory getByName(String categoryName) {
        return dao.getByName(categoryName);
    }

    public void create(BiographyCategoryRequest categoryRequest) {
        BiographyCategory category = new BiographyCategory();

        category.setName(categoryRequest.getName());
        category.setImagePath(categoryRequest.getImagePath());

        dao.create(category);
    }
}
