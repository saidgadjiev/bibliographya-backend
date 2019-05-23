package ru.saidgadjiev.bibliographya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliographya.bussiness.bug.Handler;
import ru.saidgadjiev.bibliographya.data.mapper.BibliographyaMapper;
import ru.saidgadjiev.bibliographya.domain.Bug;
import ru.saidgadjiev.bibliographya.domain.CompleteResult;
import ru.saidgadjiev.bibliographya.model.BugRequest;
import ru.saidgadjiev.bibliographya.model.BugResponse;
import ru.saidgadjiev.bibliographya.model.CompleteRequest;
import ru.saidgadjiev.bibliographya.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliographya.service.impl.BugService;

import javax.validation.Valid;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

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

    @GetMapping("")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getBugs(TimeZone timeZone,
                                     OffsetLimitPageRequest pageRequest,
                                     @RequestParam(value = "query", required = false) String query) {
        Page<Bug> page = bugService.getBugs(timeZone, pageRequest, query);

        if (page.getContent().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<BugResponse> bugResponses = modelMapper.convertToBugResponse(page.getContent());

        bugResponses.forEach(bugResponse -> {
            bugResponse.setActions(Collections.emptyList());
            bugResponse.setFixerId(null);
            bugResponse.setFixer(null);
        });

        return ResponseEntity.ok(
                new PageImpl<>(
                        bugResponses,
                        pageRequest,
                        page.getTotalElements()
                )
        );
    }

    @GetMapping("/tracking")
    @PreAuthorize("hasRole('ROLE_DEVELOPER')")
    public ResponseEntity<?> getBugsTracking(TimeZone timeZone,
                                             OffsetLimitPageRequest pageRequest,
                                             @RequestParam(value = "query", required = false) String query) {
        Page<Bug> page = bugService.getBugsTracks(timeZone, pageRequest, query);

        if (page.getContent().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(
                new PageImpl<>(
                        modelMapper.convertToBugResponse(page.getContent()),
                        pageRequest,
                        page.getTotalElements()
                )
        );
    }

    @PostMapping("")
    public ResponseEntity<?> create(TimeZone timeZone,
                                    @Valid @RequestBody BugRequest bugRequest,
                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        Bug bug = bugService.create(timeZone, bugRequest);
        BugResponse bugResponse = modelMapper.convertToBugResponse(bug);

        bugResponse.setActions(null);
        bugResponse.setFixer(null);

        return ResponseEntity.ok(bugResponse);
    }

    @PatchMapping("/{bugId}/assign-me")
    @PreAuthorize("hasRole('ROLE_DEVELOPER')")
    public ResponseEntity<?> assignMe(TimeZone timeZone,
                                      @PathVariable("bugId") int bugId,
                                      @RequestBody CompleteRequest completeRequest) throws SQLException {
        Handler.Signal signal = Handler.Signal.fromDesc(completeRequest.getSignal());

        if (signal == null || !signal.equals(Handler.Signal.ASSIGN_ME)) {
            return ResponseEntity.badRequest().build();
        }

        CompleteResult<Bug> updated = bugService.complete(
                timeZone,
                bugId,
                completeRequest
        );

        Bug bug = bugService.getFixerInfo(bugId);

        if (bug == null) {
            return ResponseEntity.notFound().build();
        }

        if (updated.getUpdated() == 0) {
            BugResponse response = modelMapper.convertToBugResponse(bug);

            response.setActions(Collections.emptyList());

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(response);
        }

        return ResponseEntity.ok(modelMapper.convertToBugResponse(bug));
    }

    @PreAuthorize("hasRole('ROLE_DEVELOPER')")
    @PatchMapping("/{bugId}/complete")
    public ResponseEntity<?> complete(
            TimeZone timeZone,
            @PathVariable("bugId") int bugId,
            @RequestBody CompleteRequest completeRequest
    ) throws SQLException {
        CompleteResult<Bug> updated = bugService.complete(
                timeZone,
                bugId,
                completeRequest
        );

        if (updated.getUpdated() == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(modelMapper.convertToBugResponse(updated.getObject()));
    }
}
