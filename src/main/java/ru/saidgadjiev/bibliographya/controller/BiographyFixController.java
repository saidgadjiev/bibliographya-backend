package ru.saidgadjiev.bibliographya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliographya.bussiness.fix.FixAction;
import ru.saidgadjiev.bibliographya.data.mapper.BibliographyaMapper;
import ru.saidgadjiev.bibliographya.domain.BiographyFix;
import ru.saidgadjiev.bibliographya.domain.CompleteResult;
import ru.saidgadjiev.bibliographya.model.CompleteRequest;
import ru.saidgadjiev.bibliographya.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliographya.service.impl.BiographyFixService;

import java.sql.SQLException;

/**
 * Created by said on 15.12.2018.
 */
@RestController
@RequestMapping("/api/fixes")
@PreAuthorize("hasRole('ROLE_MODERATOR')")
public class BiographyFixController {

    private final BiographyFixService fixService;

    private final BibliographyaMapper modelMapper;

    @Autowired
    public BiographyFixController(BiographyFixService fixService, BibliographyaMapper modelMapper) {
        this.fixService = fixService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("")
    public ResponseEntity<?> getFixes(
            OffsetLimitPageRequest pageRequest,
            @RequestParam(value = "q", required = false) String query
    ) {
        Page<BiographyFix> page = fixService.getFixesList(pageRequest, query);

        if (page.getContent().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(new PageImpl<>(modelMapper.convertToBiographyFixResponse(page.getContent()), pageRequest, page.getTotalElements()));
    }

    @PatchMapping("/{fixId}/assign-me")
    public ResponseEntity<?> assignMe(@PathVariable("fixId") int fixId, @RequestBody CompleteRequest completeRequest) throws SQLException {
        CompleteResult<BiographyFix, FixAction> updated = fixService.complete(
                fixId,
                completeRequest
        );
        BiographyFix fix = fixService.getFixerInfo(fixId);

        if (fix == null) {
            return ResponseEntity.notFound().build();
        }

        if (updated.getUpdated() == 0) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(modelMapper.convertToBiographyFixResponse(fix));
        }

        return ResponseEntity.ok(modelMapper.convertToBiographyFixResponse(fix));
    }

    @PatchMapping("/{fixId}/complete")
    public ResponseEntity<?> complete(@PathVariable("fixId") int fixId, @RequestBody CompleteRequest completeRequest) throws SQLException {
        CompleteResult<BiographyFix, FixAction> updated = fixService.complete(
                fixId,
                completeRequest
        );

        if (updated.getUpdated() == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(modelMapper.convertToBiographyFixResponse(updated.getObject()));
    }
}
