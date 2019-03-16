package ru.saidgadjiev.bibliographya.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.saidgadjiev.bibliographya.dao.impl.BiographyCategoryDao;
import ru.saidgadjiev.bibliographya.domain.BiographyCategory;
import ru.saidgadjiev.bibliographya.model.OffsetLimitPageRequest;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class BiographyCategoryServiceTest {

    @MockBean
    private BiographyCategoryDao biographyCategoryDao;

    @Autowired
    private BiographyCategoryService biographyCategoryService;

    @Test
    void getCategories() {
        BiographyCategory category = createCategory(1, "Test", "Test1.jpg");
        BiographyCategory category1 = createCategory(2, "Test1", "Test1.jpg");

        OffsetLimitPageRequest pageRequest = new OffsetLimitPageRequest.Builder()
                .setLimit(10)
                .setOffset(0)
                .setSort(Sort.unsorted())
                .build();

        List<BiographyCategory> categories = new ArrayList<>();

        categories.add(category);
        categories.add(category1);

        Mockito.when(biographyCategoryDao.getList(eq(10), eq(0L))).thenReturn(categories);

        Page<BiographyCategory> page = biographyCategoryService.getCategories(pageRequest);

        Assertions.assertEquals(2, page.getContent().size());
        assertEquals(category, page.getContent().get(0));
        assertEquals(category1, page.getContent().get(1));
    }

    @Test
    void getByName() {
        BiographyCategory category = createCategory(1, "Test", "Test.jpg");

        Mockito.when(biographyCategoryDao.getById(eq(1))).thenReturn(category);

        BiographyCategory result = biographyCategoryService.getById(1);

        Assertions.assertNotNull(result);
        assertEquals(category, result);
    }

    @Test
    void create() {
        /*List<BiographyCategory> db = new ArrayList<>();
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                BiographyCategory category = (BiographyCategory) invocation.getArguments()[0];

                category.setId(1);

                db.add(category);

                return category;
            }
        }).when(biographyCategoryDao).create(any());

        BiographyCategoryRequest request = new BiographyCategoryRequest();

        request.setName("Test");
        request.setImagePath("Test.jpg");

        BiographyCategory created = biographyCategoryService.create(request);

        Assertions.assertFalse(db.isEmpty());

        assertEquals(db.get(0), createCategory(1, "Test", "Test.jpg"));
        assertEquals(created, createCategory(1, "Test", "Test.jpg"));*/
    }

    @Test
    void deleteByName() {
        List<BiographyCategory> db = new ArrayList<>();

        db.add(createCategory(1, "Test", "Test.jpg"));

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                db.clear();

                return 1;
            }
        }).when(biographyCategoryDao).deleteById(eq(1));

        int deleted = biographyCategoryService.deleteById(1);

        Assertions.assertEquals(deleted, 1);
        Assertions.assertTrue(db.isEmpty());
    }

    @Test
    void update() {
        /*List<BiographyCategory> db = new ArrayList<>();

        db.add(createCategory(1, "Test", "Test.jpg"));

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                BiographyCategory category = (BiographyCategory) invocation.getArguments()[0];

                db.get(0).setName(category.getName());
                db.get(0).setImagePath(category.getImagePath());

                return 1;
            }
        }).when(biographyCategoryDao).update(any());

        BiographyCategoryRequest request = new BiographyCategoryRequest();

        request.setName("Test1");
        request.setImagePath("Test1.jpg");

        int updated = biographyCategoryService.update(1, request);

        Assertions.assertEquals(updated, 1);
        assertEquals(db.get(0), createCategory(1, "Test1", "Test1.jpg"));*/
    }

    private void assertEquals(BiographyCategory expected, BiographyCategory actual) {
        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.getName(), actual.getName());
        Assertions.assertEquals(expected.getImagePath(), actual.getImagePath());
    }

    private BiographyCategory createCategory(int id, String name, String imagePath) {
        BiographyCategory category = new BiographyCategory();

        category.setId(id);
        category.setName(name);
        category.setImagePath(imagePath);

        return category;
    }
}