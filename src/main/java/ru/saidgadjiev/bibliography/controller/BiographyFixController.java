package ru.saidgadjiev.bibliography.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliography.bussiness.fix.FixAction;
import ru.saidgadjiev.bibliography.bussiness.moderation.ModerationAction;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.BiographyFix;
import ru.saidgadjiev.bibliography.domain.CompleteResult;
import ru.saidgadjiev.bibliography.model.*;
import ru.saidgadjiev.bibliography.service.impl.BiographyFixService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by said on 15.12.2018.
 */
@RestController
@RequestMapping("/api/fixes")
@PreAuthorize("hasRole('ROLE_MODERATOR')")
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
            OffsetLimitPageRequest pageRequest,
            @RequestParam(value = "q", required = false) String query
    ) {
        Page<BiographyFix> page = fixService.getFixesList(pageRequest, query);

        if (page.getContent().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(new PageImpl<>(convertToDto(page.getContent()), pageRequest, page.getTotalElements()));
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
            return ResponseEntity.status(HttpStatus.CONFLICT).body(convertToDto(fix, Collections.emptyList()));
        }

        return ResponseEntity.ok(convertToDto(fix, updated.getActions()));
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

        return ResponseEntity.ok(convertToDto(updated.getObject(), updated.getActions()));
    }

    private List<BiographyFixResponse> convertToDto(Collection<BiographyFix> biographyFixes) {
        List<BiographyFixResponse> result = new ArrayList<>();

        for (BiographyFix fix: biographyFixes) {
            BiographyFixResponse response = modelMapper.map(fix, BiographyFixResponse.class);

            response.setActions(fixService.getActions(fix));
            result.add(response);
        }

        return result;
    }

    private BiographyFixResponse convertToDto(BiographyFix biographyFix, Collection<FixAction> actions) {
        BiographyFixResponse response = modelMapper.map(biographyFix, BiographyFixResponse.class);

        response.setActions(actions);

        return response;
    }
}
