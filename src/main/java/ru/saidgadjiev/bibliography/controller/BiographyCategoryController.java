package ru.saidgadjiev.bibliography.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.BiographyCategory;
import ru.saidgadjiev.bibliography.model.BiographyResponse;
import ru.saidgadjiev.bibliography.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliography.service.impl.BiographyCategoryService;
import ru.saidgadjiev.bibliography.service.impl.BiographyService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by said on 27.11.2018.
 */
@RestController
@RequestMapping("/api/categories")
public class BiographyCategoryController {

    private final ModelMapper modelMapper;

    private final BiographyCategoryService biographyCategoryService;

    private final BiographyService biographyService;

    @Autowired
    public BiographyCategoryController(ModelMapper modelMapper,
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
    public ResponseEntity<BiographyCategory> getCategoryById(@PathVariable("categoryName") String categoryName) {
        BiographyCategory category = biographyCategoryService.getByName(categoryName);

        if (category == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(category);
    }

    @GetMapping("/{categoryName}/biographies")
    public ResponseEntity<Page<BiographyResponse>> getBiographies(
            @PathVariable("categoryName") String categoryName,
            OffsetLimitPageRequest pageRequest
    ) {
        Page<Biography> page = biographyService.getBiographies(pageRequest, categoryName);

        if (page.getContent().size() == 0) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(new PageImpl<>(convertToDto(page.getContent()), page.getPageable(), page.getTotalElements()));
    }


    private List<BiographyResponse> convertToDto(List<Biography> biographies) {
        List<BiographyResponse> dto = new ArrayList<>();

        for (Biography biography : biographies) {
            BiographyResponse biographyResponse = modelMapper.map(biography, BiographyResponse.class);

            dto.add(biographyResponse);
        }

        return dto;
    }
}
