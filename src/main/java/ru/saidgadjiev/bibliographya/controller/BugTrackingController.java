package ru.saidgadjiev.bibliographya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliographya.bussiness.moderation.ModerationAction;
import ru.saidgadjiev.bibliographya.data.mapper.BibliographyaMapper;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.CompleteResult;
import ru.saidgadjiev.bibliographya.model.BugRequest;
import ru.saidgadjiev.bibliographya.model.CompleteRequest;
import ru.saidgadjiev.bibliographya.service.impl.BiographyService;
import ru.saidgadjiev.bibliographya.service.impl.BugService;

import java.sql.SQLException;

@RestController
@RequestMapping("/api/bugs")
public class BugTrackingController {

    private final BugService bugService;

    private final BiographyService biographyService;

    private BibliographyaMapper modelMapper;

    @Autowired
    public BugTrackingController(BugService bugService, BiographyService biographyService, BibliographyaMapper modelMapper) {
        this.bugService = bugService;
        this.biographyService = biographyService;
        this.modelMapper = modelMapper;
    }

    @PostMapping("")
    public ResponseEntity<?> create(@RequestBody BugRequest bugRequest) {
        bugService.create(bugRequest);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{bugId}/assign-me")
    public ResponseEntity<?> assignMe(@PathVariable("bugId") int bugId, @RequestBody CompleteRequest completeRequest) {
        CompleteResult<Biography, ModerationAction> updated = bugService.complete(
                bugId,
                completeRequest
        );
        Biography biography = biographyService.getCurrentUserShortBiography();

        if (biography == null) {
            return ResponseEntity.notFound().build();
        }

        if (updated.getUpdated() == 0) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(modelMapper.convertToBiographyModerationResponse(biography));
        }

        return ResponseEntity.ok(modelMapper.convertToBiographyModerationResponse(biography));
    }

    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    @PatchMapping("/{bugId}/complete")
    public ResponseEntity<?> complete(
            @PathVariable("bugId") int bugId,
            @RequestBody CompleteRequest completeRequest
    ) throws SQLException {
        CompleteResult<Biography, ModerationAction> updated = bugService.complete(
                bugId,
                completeRequest
        );

        if (updated.getUpdated() == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(modelMapper.convertToBiographyModerationResponse(updated.getObject()));
    }
}
