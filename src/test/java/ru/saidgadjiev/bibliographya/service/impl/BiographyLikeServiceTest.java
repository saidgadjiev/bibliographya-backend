package ru.saidgadjiev.bibliographya.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.saidgadjiev.bibliographya.dao.impl.BiographyLikeDaoImpl;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.BiographyLike;
import ru.saidgadjiev.bibliographya.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliographya.utils.TestAssertionsUtils;
import ru.saidgadjiev.bibliographya.utils.TestModelsUtils;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class BiographyLikeServiceTest {

    @MockBean
    private SecurityService securityService;

    @MockBean
    private BiographyLikeDaoImpl biographyLikeDao;

    @Autowired
    private BiographyLikeService biographyLikeService;

    @Test
    void like() {
        authenticate();

        List<BiographyLike> db = new ArrayList<>();

        Mockito.doAnswer(invocation -> {
            BiographyLike like = (BiographyLike) invocation.getArguments()[0];

            db.add(like);

            return null;
        }).when(biographyLikeDao).create(any(BiographyLike.class));

        biographyLikeService.like(1);

        Assertions.assertEquals(1, db.size());

        BiographyLike like = new BiographyLike(1, 1);

        TestAssertionsUtils.assertLikeEquals(like, db.get(0));
    }

    @Test
    void unlike() {
        authenticate();

        List<BiographyLike> db = new ArrayList<>();

        BiographyLike like = new BiographyLike(1, 1);

        db.add(like);

        Mockito.doAnswer(invocation -> {
            BiographyLike remove = (BiographyLike) invocation.getArguments()[0];

            db.removeIf(biographyLike -> biographyLike.getUserId().equals(remove.getUserId())
                    && biographyLike.getBiographyId().equals(remove.getBiographyId()));

            return null;
        }).when(biographyLikeDao).delete(any(BiographyLike.class));

        biographyLikeService.unlike(1);

        Assertions.assertEquals(0, db.size());
    }

    @Test
    void getBiographyLikes() {
        OffsetLimitPageRequest pageRequest = new OffsetLimitPageRequest.Builder()
                .setLimit(10)
                .setOffset(0)
                .setSort(Sort.unsorted())
                .build();

        List<BiographyLike> likes = new ArrayList<>();

        BiographyLike like = new BiographyLike();
        Biography biography = new Biography();

        biography.setId(1);
        biography.setFirstName(TestModelsUtils.TEST_FIRST_NAME);
        biography.setLastName(TestModelsUtils.TEST_LAST_NAME);

        like.setUser(biography);

        likes.add(like);

        Mockito.when(biographyLikeDao.getLikes(eq(1), eq(10), eq(0L))).thenReturn(likes);

        Page<BiographyLike> page = biographyLikeService.getBiographyLikes(1, pageRequest);

        Assertions.assertEquals(1, page.getContent().size());
        TestAssertionsUtils.assertLikeEquals(page.getContent().get(0), like);
    }

    private void authenticate() {
        Mockito.when(securityService.findLoggedInUser()).thenReturn(TestModelsUtils.TEST_USERS.get(TestModelsUtils.TEST_FACEBOOK_USER_ID));
    }
}