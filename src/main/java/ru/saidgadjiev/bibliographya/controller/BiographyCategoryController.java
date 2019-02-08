package ru.saidgadjiev.bibliographya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliographya.data.mapper.BibliographyaMapper;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.BiographyCategory;
import ru.saidgadjiev.bibliographya.model.BiographyCategoryRequest;
import ru.saidgadjiev.bibliographya.model.BiographyResponse;
import ru.saidgadjiev.bibliographya.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliographya.service.impl.BiographyCategoryBiographyService;
import ru.saidgadjiev.bibliographya.service.impl.BiographyCategoryService;
import ru.saidgadjiev.bibliographya.service.impl.BiographyService;

import javax.validation.Valid;

/**
 * Created by said on 27.11.2018.
 */
@RestController
@RequestMapping("/api/categories")
public class BiographyCategoryController {

    private final BibliographyaMapper modelMapper;

    private final BiographyCategoryService biographyCategoryService;

    private final BiographyService biographyService;

    @Autowired
    public BiographyCategoryController(BibliographyaMapper modelMapper,
                                       BiographyCategoryService biographyCategoryService,
                                       BiographyService biographyService) {
        this.modelMapper = modelMapper;
        this.biographyCategoryService = biographyCategoryService;
        this.biographyService = biographyService;
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
    public ResponseEntity<BiographyCategory> getCategoryByName(@PathVariable("categoryName") String categoryName) {
        BiographyCategory category = biographyCategoryService.getByName(categoryName);

        if (category == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(category);
    }

    @GetMapping("/{categoryName}/biographies")
    public ResponseEntity<Page<BiographyResponse>> getBiographies(
            @PathVariable("categoryName") String categoryName,
            OffsetLimitPageRequest pageRequest,
            @RequestParam(value = "autobiographies", required = false) Boolean autobiographies
    ) {
        Page<Biography> page = biographyService.getBiographies(pageRequest, categoryName, autobiographies);

        if (page.getContent().size() == 0) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(
                new PageImpl<>(
                        modelMapper.convertToBiographyResponse(page.getContent()),
                        page.getPageable(),
                        page.getTotalElements()
                )
        );
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> create(@RequestBody BiographyCategoryRequest categoryRequest) {
        BiographyCategory category = biographyCategoryService.create(categoryRequest);

        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/{categoryName}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable("categoryName") String categoryName) {
        int deleted = biographyCategoryService.deleteByName(categoryName);

        if (deleted == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> update(
            @PathVariable("id") int id,
            @Valid @RequestBody BiographyCategoryRequest biographyCategoryRequest,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        int updated = biographyCategoryService.update(id, biographyCategoryRequest);

        if (updated == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }
}
