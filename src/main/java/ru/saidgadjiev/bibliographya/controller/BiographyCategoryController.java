package ru.saidgadjiev.bibliographya.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.saidgadjiev.bibliographya.data.mapper.BibliographyaMapper;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.BiographyCategory;
import ru.saidgadjiev.bibliographya.model.BiographyCategoryRequest;
import ru.saidgadjiev.bibliographya.model.BiographyResponse;
import ru.saidgadjiev.bibliographya.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliographya.service.api.StorageService;
import ru.saidgadjiev.bibliographya.service.impl.BiographyCategoryService;
import ru.saidgadjiev.bibliographya.service.impl.BiographyService;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import java.io.IOException;
import java.util.Set;
import java.util.TimeZone;

/**
 * Created by said on 27.11.2018.
 */
@RestController
@RequestMapping("/api/categories")
public class BiographyCategoryController {

    private final BibliographyaMapper modelMapper;

    private final BiographyCategoryService biographyCategoryService;

    private final BiographyService biographyService;

    private final SmartValidator validator;

    private final ObjectMapper objectMapper;

    @Autowired
    public BiographyCategoryController(BibliographyaMapper modelMapper,
                                       BiographyCategoryService biographyCategoryService,
                                       BiographyService biographyService,
                                       SmartValidator validator,
                                       ObjectMapper objectMapper) {
        this.modelMapper = modelMapper;
        this.biographyCategoryService = biographyCategoryService;
        this.biographyService = biographyService;
        this.validator = validator;
        this.objectMapper = objectMapper;
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

    @GetMapping("/{id:[\\d]+}")
    public ResponseEntity<BiographyCategory> getById(@PathVariable("id") Integer id) {
        BiographyCategory category = biographyCategoryService.getById(id);

        if (category == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(category);
    }

    @GetMapping("/{id:[\\d]+}/biographies")
    public ResponseEntity<Page<BiographyResponse>> getBiographies(
            TimeZone timeZone,
            @PathVariable("id") Integer id,
            OffsetLimitPageRequest pageRequest,
            @RequestParam(value = "autobiographies", required = false) Boolean autobiographies
    ) {
        Page<Biography> page = biographyService.getBiographies(timeZone, pageRequest, id, autobiographies);

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
    public ResponseEntity<?> create(@RequestPart(value = "file") MultipartFile file,
                                    @RequestPart("data") String data,
                                    BindingResult result) throws IOException {
        BiographyCategoryRequest categoryRequest = objectMapper.readValue(data, BiographyCategoryRequest.class);

        validator.validate(categoryRequest, result, BiographyCategoryRequest.class);

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        BiographyCategory category = biographyCategoryService.create(categoryRequest, file);

        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/{id:[\\d]+}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable("id") Integer id) {
        int deleted = biographyCategoryService.deleteById(id);

        if (deleted == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id:[\\d]+}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> update(
            @PathVariable("id") int id,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "data", required = false) String data,
            BindingResult bindingResult
    ) throws IOException {
        BiographyCategoryRequest categoryRequest = null;

        if (StringUtils.isNotBlank(data)) {
            categoryRequest = objectMapper.readValue(data, BiographyCategoryRequest.class);

            validator.validate(categoryRequest, bindingResult, BiographyCategoryRequest.class);

            if (bindingResult.hasErrors()) {
                return ResponseEntity.badRequest().build();
            }
        }

        BiographyCategory updated = biographyCategoryService.update(id, categoryRequest, file);

        if (updated == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updated);
    }
}
