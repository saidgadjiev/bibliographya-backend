package ru.saidgadjiev.bibliographya.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.saidgadjiev.bibliographya.domain.BiographyCategory;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.model.BiographyCategoryRequest;
import ru.saidgadjiev.bibliographya.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliographya.service.impl.BiographyCategoryService;
import ru.saidgadjiev.bibliographya.service.impl.auth.AuthService;

import javax.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class BiographyCategoryControllerTest {

    @MockBean
    private BiographyCategoryService biographyCategoryService;

    @MockBean
    private AuthService authService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getCategories() throws Exception {
        BiographyCategory category = createCategory(1, "Test", "Test.jpg");
        BiographyCategory category1 = createCategory(2, "Test1", "Test1.jpg");

        List<BiographyCategory> categories = new ArrayList<>();

        categories.add(category);
        categories.add(category1);

        OffsetLimitPageRequest pageRequest = new OffsetLimitPageRequest.Builder()
                .setLimit(10)
                .setOffset(0)
                .setSort(Sort.unsorted())
                .build();

        Mockito.when(biographyCategoryService.getCategories(pageRequest)).thenReturn(new PageImpl<>(categories, pageRequest, 2));

        mockMvc.perform(get("/api/categories?limit=10&offset=0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].name", is("Test")))
                .andExpect(jsonPath("$.content[0].imagePath", is(category.getImagePath())))
                .andExpect(jsonPath("$.content[1].id", is(category1.getId())))
                .andExpect(jsonPath("$.content[1].name", is(category1.getName())))
                .andExpect(jsonPath("$.content[1].imagePath", is(category1.getImagePath())));
    }

    @Test
    void getCategoryByName() throws Exception {
        BiographyCategory category = createCategory(1, "Test", "Test.jpg");

        Mockito.when(biographyCategoryService.getByName("Test")).thenReturn(category);

        mockMvc.perform(get("/api/categories/Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test")))
                .andExpect(jsonPath("$.imagePath", is("Test.jpg")));
    }

    @Test
    void getBiographies() {

    }

    @Test
    void create() throws Exception {
        List<BiographyCategory> db = new ArrayList<>();

        Mockito.doAnswer(invocation -> {
            BiographyCategoryRequest categoryRequest = (BiographyCategoryRequest) invocation.getArguments()[0];

            BiographyCategory category = new BiographyCategory();

            category.setName(categoryRequest.getName());
            category.setImagePath(categoryRequest.getImagePath());
            category.setId(1);

            db.add(category);

            return category;
        }).when(biographyCategoryService).create(any());

        ObjectMapper objectMapper = new ObjectMapper();

        BiographyCategoryRequest request = new BiographyCategoryRequest();

        request.setName("Test");
        request.setImagePath("Test.jpg");

        Mockito.doAnswer(invocationOnMock -> {
            authenticate();

            return null;
        }).when(authService).tokenAuth("TestToken");

        mockMvc.perform(
                post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(objectMapper.writeValueAsString(request))
                        .cookie(new Cookie("X-TOKEN", "TestToken"))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test")))
                .andExpect(jsonPath("$.imagePath", is("Test.jpg")));

        Assertions.assertFalse(db.isEmpty());
        Assertions.assertEquals(db.get(0).getId(), 1);
        Assertions.assertEquals(db.get(0).getName(), request.getName());
        Assertions.assertEquals(db.get(0).getImagePath(), request.getImagePath());

        logout();
    }

    @Test
    void delete() throws Exception {
        List<BiographyCategory> db = new ArrayList<>();

        db.add(createCategory(1, "Test", "Test.jpg"));

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                db.clear();

                return 1;
            }
        }).when(biographyCategoryService).deleteByName(eq("Test"));

        Mockito.doAnswer(invocationOnMock -> {
            authenticate();

            return null;
        }).when(authService).tokenAuth("TestToken");

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/categories/Test")
                        .cookie(new Cookie("X-TOKEN", "TestToken"))
        )
                .andExpect(status().isOk());

        Assertions.assertTrue(db.isEmpty());

        logout();
    }

    @Test
    void update() throws Exception {
        List<BiographyCategory> db = new ArrayList<>();

        db.add(createCategory(1, "Test", "Test.jpg"));

        Mockito.doAnswer(new Answer() {
            @Override
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                BiographyCategoryRequest categoryRequest = (BiographyCategoryRequest) invocation.getArguments()[1];

                db.get(0).setName(categoryRequest.getName());
                db.get(0).setImagePath(categoryRequest.getImagePath());

                return 1;
            }
        }).when(biographyCategoryService).update(eq(1), any());

        BiographyCategoryRequest request = new BiographyCategoryRequest();

        request.setName("Test1");
        request.setImagePath("Test1.jpg");

        ObjectMapper objectMapper = new ObjectMapper();

        Mockito.doAnswer(invocationOnMock -> {
            authenticate();

            return null;
        }).when(authService).tokenAuth("TestToken");

        mockMvc.perform(
                put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(objectMapper.writeValueAsString(request))
                        .cookie(new Cookie("X-TOKEN", "TestToken"))
        )
                .andExpect(status().isOk());

        Assertions.assertEquals(db.get(0).getName(), request.getName());
        Assertions.assertEquals(db.get(0).getImagePath(), request.getImagePath());

        logout();
    }

    private BiographyCategory createCategory(int id, String name, String imagePath) {
        BiographyCategory category = new BiographyCategory();

        category.setId(id);
        category.setName(name);
        category.setImagePath(imagePath);

        return category;
    }

    private void authenticate() {
        User user = new User();

        user.setId(1);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, Stream
                        .of("ROLE_ADMIN")
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void logout() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }
}