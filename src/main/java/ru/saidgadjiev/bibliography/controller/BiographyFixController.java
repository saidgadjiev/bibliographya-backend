package ru.saidgadjiev.bibliography.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliography.domain.BiographyFix;
import ru.saidgadjiev.bibliography.model.BiographyFixResponse;
import ru.saidgadjiev.bibliography.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliography.service.impl.BiographyFixService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by said on 15.12.2018.
 */
@RestController
@RequestMapping("/api/biography/fix")
public class BiographyFixController {

    private final BiographyFixService fixService;

    private final ModelMapper modelMapper;

    @Autowired
    public BiographyFixController(BiographyFixService fixService, ModelMapper modelMapper) {
        this.fixService = fixService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("")
    public ResponseEntity<?> getFixes(
            OffsetLimitPageRequest pageRequest
    ) {
        Page<BiographyFix> page = fixService.getFixesList(pageRequest);

        if (page.getContent().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(new PageImpl<>(convertToDto(page.getContent()), pageRequest, page.getTotalElements()));
    }

    @PatchMapping("/assign-me/{id}")
    public ResponseEntity<?> assignMe(@PathVariable("id") int fixId) {
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/close/{id}")
    public ResponseEntity<?> close(@PathVariable("id") int fixId) {
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/pending/{id}")
    public ResponseEntity<?> pending(@PathVariable("id") int fixId) {
        return ResponseEntity.ok().build();
    }

    private List<BiographyFixResponse> convertToDto(Collection<BiographyFix> biographyFixes) {
        List<BiographyFixResponse> result = new ArrayList<>();

        for (BiographyFix fix: biographyFixes) {
            result.add(modelMapper.map(fix, BiographyFixResponse.class));
        }

        return result;
    }
}
