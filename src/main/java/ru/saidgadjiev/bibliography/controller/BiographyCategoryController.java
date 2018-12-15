package ru.saidgadjiev.bibliography.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.saidgadjiev.bibliography.domain.BiographyCategory;
import ru.saidgadjiev.bibliography.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliography.service.impl.BiographyCategoryService;

/**
 * Created by said on 27.11.2018.
 */
@RestController
@RequestMapping("/api/biography/category")
public class BiographyCategoryController {

    private final BiographyCategoryService biographyCategoryService;

    @Autowired
    public BiographyCategoryController(BiographyCategoryService biographyCategoryService) {
        this.biographyCategoryService = biographyCategoryService;
    }

    @GetMapping("")
    public ResponseEntity<?> getCategories(
            OffsetLimitPageRequest pageRequest
    ) {
        Page<BiographyCategory> page = biographyCategoryService.getCategories(pageRequest);

        if (page.getContent().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(page);
    }

    @GetMapping("/{categoryName}")
    public ResponseEntity<BiographyCategory> getCategoryById(@PathVariable("categoryName") String categoryName) {
        BiographyCategory category = biographyCategoryService.getByName(categoryName);

        if (category == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(category);
    }
}
