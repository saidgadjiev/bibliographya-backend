package ru.saidgadjiev.bibliography.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.model.BiographyResponse;
import ru.saidgadjiev.bibliography.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliography.service.impl.BiographyModerationService;
import ru.saidgadjiev.bibliography.service.impl.BiographyService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by said on 25.11.2018.
 */
@RestController
@RequestMapping("/api/biography/moderation")
public class BiographyModerationController {

    private final BiographyModerationService biographyModerationService;

    private final ModelMapper modelMapper;

    @Autowired
    public BiographyModerationController(BiographyModerationService biographyModerationService,
                                         ModelMapper modelMapper) {
        this.biographyModerationService = biographyModerationService;
        this.modelMapper = modelMapper;
    }

    @GetMapping(value = "")
    public ResponseEntity<Page<BiographyResponse>> getBiographies(
            OffsetLimitPageRequest pageRequest,
            @RequestParam(value = "moderatorName", required = false) String moderatorNameFilter,
            @RequestParam(value = "moderationStatus", required = false) String moderationStatusFilter
    ) throws SQLException {
        Page<Biography> page = biographyModerationService.getBiographies(
                pageRequest,
                moderatorNameFilter,
                moderationStatusFilter
        );

        if (page.getContent().size() == 0) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(new PageImpl<>(convertToDto(page.getContent()), page.getPageable(), page.getTotalElements()));
    }

    @PatchMapping("/assign-me/{id}")
    public ResponseEntity<?> assignMe(@PathVariable("id") int biographyId) {
        int updated = biographyModerationService.assignMe(biographyId);
        Biography biography = biographyModerationService.getModeratorInfo(biographyId);

        if (biography == null) {
            return ResponseEntity.notFound().build();
        }

        if (updated == 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(convertToDto(biography));
        }

        return ResponseEntity.ok(convertToDto(biography));
    }

    @PatchMapping("/release/{id}")
    public ResponseEntity<?> release(@PathVariable("id") int biographyId) {
        int updated = biographyModerationService.release(biographyId);

        if (updated == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/approve/{id}")
    public ResponseEntity<?> approve(@PathVariable("id") int biographyId) {
        int updated = biographyModerationService.approve(biographyId);

        if (updated == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/reject/{id}")
    public ResponseEntity<?> reject(@PathVariable("id") int biographyId) {
        int updated = biographyModerationService.reject(biographyId);

        if (updated == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/pending/{id}")
    public ResponseEntity<?> pending(@PathVariable("id") int biographyId) {
        int updated = biographyModerationService.pending(biographyId);

        if (updated == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }

    private List<BiographyResponse> convertToDto(List<Biography> biographies) {
        List<BiographyResponse> dto = new ArrayList<>();

        for (Biography biography : biographies) {
            BiographyResponse biographyResponse = modelMapper.map(biography, BiographyResponse.class);

            dto.add(biographyResponse);
        }

        return dto;
    }

    private BiographyResponse convertToDto(Biography biography) {
        return modelMapper.map(biography, BiographyResponse.class);
    }
}
