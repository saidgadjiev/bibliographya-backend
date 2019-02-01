package ru.saidgadjiev.bibliographya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliographya.bussiness.bug.BugAction;
import ru.saidgadjiev.bibliographya.data.mapper.BibliographyaMapper;
import ru.saidgadjiev.bibliographya.domain.Bug;
import ru.saidgadjiev.bibliographya.domain.CompleteResult;
import ru.saidgadjiev.bibliographya.model.BugRequest;
import ru.saidgadjiev.bibliographya.model.CompleteRequest;
import ru.saidgadjiev.bibliographya.service.impl.BugService;

import java.sql.SQLException;

@RestController
@RequestMapping("/api/bugs")
public class BugTrackingController {

    private final BugService bugService;

    private BibliographyaMapper modelMapper;

    @Autowired
    public BugTrackingController(BugService bugService, BibliographyaMapper modelMapper) {
        this.bugService = bugService;
        this.modelMapper = modelMapper;
    }

    @PostMapping("")
    public ResponseEntity<?> create(@RequestBody BugRequest bugRequest) {
        bugService.create(bugRequest);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{bugId}/assign-me")
    public ResponseEntity<?> assignMe(@PathVariable("bugId") int bugId, @RequestBody CompleteRequest completeRequest) throws SQLException {
        CompleteResult<Bug, BugAction> updated = bugService.complete(
                bugId,
                completeRequest
        );

        Bug bug = bugService.getFixerInfo(bugId);

        if (bug == null) {
            return ResponseEntity.notFound().build();
        }

        if (updated.getUpdated() == 0) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(modelMapper.convertToBugResponse(bug));
        }

        return ResponseEntity.ok(modelMapper.convertToBugResponse(bug));
    }

    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    @PatchMapping("/{bugId}/complete")
    public ResponseEntity<?> complete(
            @PathVariable("bugId") int bugId,
            @RequestBody CompleteRequest completeRequest
    ) throws SQLException {
        CompleteResult<Bug, BugAction> updated = bugService.complete(
                bugId,
                completeRequest
        );

        if (updated.getUpdated() == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(modelMapper.convertToBugResponse(updated.getObject()));
    }
}
