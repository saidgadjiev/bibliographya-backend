package ru.saidgadjiev.bibliographya.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.saidgadjiev.bibliographya.dao.impl.BiographyDao;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class BiographyServiceTest {

    @Autowired
    private BiographyService biographyService;

    @MockBean
    private BiographyDao biographyDao;

    @Test
    void create() {
    }

    @Test
    void createAccountBiography() {
    }

    @Test
    void getBiographyById() {
    }

    @Test
    void getBiographies() {
    }

    @Test
    void getBiographies1() {
    }

    @Test
    void getMyBiographies() {
    }

    @Test
    void update() {
    }

    @Test
    void delete() {
    }

    @Test
    void publish() {
    }

    @Test
    void unpublish() {
    }

    @Test
    void isIAuthor() {
    }

    @Test
    void getStats() {
    }

    @Test
    void partialUpdate() {
        /*BiographyUpdateRequest request = new BiographyUpdateRequest();

        request.setAnonymousCreator(true);
        request.setDisableComments(true);
        request.setReturnFields(Collections.singleton(BiographyBaseResponse.CREATOR_ID));

        biographyService.partialUpdate(TimeZone.getTimeZone("Europe/Moscow"), 1, request);*/
    }
}