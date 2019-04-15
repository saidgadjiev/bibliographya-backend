package ru.saidgadjiev.bibliographya.service.impl;

import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.saidgadjiev.bibliographya.dao.impl.BiographyCategoryDao;
import ru.saidgadjiev.bibliographya.dao.impl.GeneralDao;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.FilterOperation;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.domain.BiographyCategory;
import ru.saidgadjiev.bibliographya.model.BiographyCategoryRequest;
import ru.saidgadjiev.bibliographya.service.api.StorageService;
import ru.saidgadjiev.bibliographya.utils.FileNameUtils;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by said on 27.11.2018.
 */
@Service
public class BiographyCategoryService {

    private StorageService storageService;

    private final BiographyCategoryDao dao;

    private GeneralDao generalDao;

    @Autowired
    public BiographyCategoryService(StorageService storageService, BiographyCategoryDao dao, GeneralDao generalDao) {
        this.storageService = storageService;
        this.dao = dao;
        this.generalDao = generalDao;
    }

    public Page<BiographyCategory> getCategories(Pageable pageRequest) {
        List<BiographyCategory> categories = dao.getList(pageRequest.getPageSize(), pageRequest.getOffset());

        return new PageImpl<>(categories, pageRequest, categories.size());
    }

    @Nullable
    public BiographyCategory getById(int id) {
        return dao.getById(id);
    }

    public BiographyCategory create(BiographyCategoryRequest categoryRequest, MultipartFile file) {
        Collection<UpdateValue> values = new ArrayList<>();

        values.add(
                new UpdateValue<>(
                        BiographyCategory.NAME,
                        categoryRequest.getName(),
                        PreparedStatement::setString
                )
        );

        KeyHolder keyHolder = generalDao.create(BiographyCategory.TABLE, values);
        int id = (int) keyHolder.getKeys().get(BiographyCategory.ID);

        String filePath = FileNameUtils.categoryUploadPath(id, file);

        storageService.store(filePath, file);

        Collection<UpdateValue> filePathUpdateValues = new ArrayList<>();

        filePathUpdateValues.add(
                new UpdateValue<>(
                        BiographyCategory.IMAGE_PATH,
                        filePath,
                        PreparedStatement::setString
                )
        );

        Collection<FilterCriteria> criteria = new ArrayList<>();

        criteria.add(
                new FilterCriteria.Builder<Integer>()
                        .propertyName(BiographyCategory.ID)
                        .valueSetter(PreparedStatement::setInt)
                        .needPreparedSet(true)
                        .filterValue(id)
                        .filterOperation(FilterOperation.EQ)
                        .build()
        );

        generalDao.update(BiographyCategory.TABLE, filePathUpdateValues, criteria, null);

        BiographyCategory category = new BiographyCategory();

        category.setId(id);
        category.setName(categoryRequest.getName());
        category.setImagePath(filePath);

        return category;
    }

    public int deleteById(int id) {
        BiographyCategory category = dao.getById(id);

        storageService.deleteResource(category.getImagePath());

        return dao.deleteById(id);
    }

    public BiographyCategory update(int id, BiographyCategoryRequest categoryRequest, MultipartFile file) {
        BiographyCategory actual = dao.getById(id);

        if (actual == null) {
            return null;
        }
        Collection<UpdateValue> values = new ArrayList<>();

        if (categoryRequest != null && categoryRequest.getName() != null) {
            values.add(
                    new UpdateValue<>(
                            BiographyCategory.NAME,
                            categoryRequest.getName(),
                            PreparedStatement::setString
                    )
            );
            actual.setName(categoryRequest.getName());
        }

        if (file != null) {
            storageService.deleteResource(actual.getImagePath());
            String filePath = FileNameUtils.categoryUploadPath(id, file);

            storageService.store(filePath, file);

            values.add(
                    new UpdateValue<>(
                            BiographyCategory.IMAGE_PATH,
                            filePath,
                            PreparedStatement::setString
                    )
            );

            actual.setImagePath(filePath);
        }

        Collection<FilterCriteria> criteria = new ArrayList<>();

        criteria.add(
                new FilterCriteria.Builder<Integer>()
                        .propertyName(BiographyCategory.ID)
                        .valueSetter(PreparedStatement::setInt)
                        .needPreparedSet(true)
                        .filterValue(id)
                        .filterOperation(FilterOperation.EQ)
                        .build()
        );

        generalDao.update(BiographyCategory.TABLE, values, criteria, null);

        return actual;
    }
}
