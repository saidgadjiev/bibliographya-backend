package ru.saidgadjiev.bibliographya.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliographya.bussiness.moderation.Handler;
import ru.saidgadjiev.bibliographya.data.mapper.BibliographyaMapper;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.BiographyComment;
import ru.saidgadjiev.bibliographya.domain.BiographyUpdateStatus;
import ru.saidgadjiev.bibliographya.domain.CompleteResult;
import ru.saidgadjiev.bibliographya.model.*;
import ru.saidgadjiev.bibliographya.service.impl.BiographyCommentService;
import ru.saidgadjiev.bibliographya.service.impl.BiographyFixService;
import ru.saidgadjiev.bibliographya.service.impl.BiographyModerationService;
import ru.saidgadjiev.bibliographya.service.impl.BiographyService;

import javax.script.ScriptException;
import javax.validation.Valid;
import java.sql.SQLException;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by said on 22.10.2018.
 */

@RestController
@RequestMapping("/api/biographies")
public class BiographyController {

    private final BiographyService biographyService;

    private final BiographyModerationService biographyModerationService;

    private final BiographyCommentService biographyCommentService;

    private final BiographyFixService fixService;

    private final BibliographyaMapper modelMapper;

    @Autowired
    public BiographyController(BiographyService biographyService,
                               BiographyModerationService biographyModerationService,
                               BiographyCommentService biographyCommentService,
                               BiographyFixService fixService,
                               BibliographyaMapper modelMapper
    ) {
        this.biographyService = biographyService;
        this.biographyModerationService = biographyModerationService;
        this.biographyCommentService = biographyCommentService;
        this.fixService = fixService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/{id:[\\d]+}")
    public ResponseEntity<BiographyResponse> getBiographyById(TimeZone timeZone, @PathVariable("id") int id) {
        Biography biography = biographyService.getBiographyById(timeZone, id);

        if (biography == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(modelMapper.convertToBiographyResponse(biography));
    }

    @GetMapping(value = "")
    public ResponseEntity<Page<BiographyResponse>> getBiographies(
            TimeZone timeZone,
            OffsetLimitPageRequest pageRequest,
            @RequestParam(value = "autobiographies", required = false) Boolean autobiographies,
            @RequestParam(value = "biographyClampSize", required = false) Integer biographyClampSize
    ) throws ScriptException, NoSuchMethodException {
        Page<Biography> page = biographyService.getBiographies(
                timeZone,
                pageRequest,
                null,
                autobiographies,
                biographyClampSize
        );

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

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/my")
    public ResponseEntity<Page<MyBiographyResponse>> getMyBiographies(
            TimeZone timeZone,
            OffsetLimitPageRequest pageRequest,
            @RequestParam(value = "biographyClampSize", required = false) Integer biographyClampSize
    ) throws ScriptException, NoSuchMethodException {
        Page<Biography> page = biographyService.getMyBiographies(timeZone, pageRequest, biographyClampSize);

        if (page.getContent().size() == 0) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(
                new PageImpl<>(modelMapper.convertToMyBiographyResponse(page.getContent()),
                        page.getPageable(),
                        page.getTotalElements()
                )
        );
    }

    @PreAuthorize("isAuthenticated() and (@biography.isIAuthor(#id) or hasAnyRole('ROLE_MODERATOR'))")
    @PutMapping(value = "/{id:[\\d]+}")
    public ResponseEntity<?> update(
            TimeZone timeZone,
            @PathVariable("id") Integer id,
            @Valid @RequestBody BiographyRequest biographyRequest,
            BindingResult bindingResult
    ) throws SQLException {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        BiographyUpdateStatus updateResult = biographyService.update(timeZone, id, biographyRequest);

        if (updateResult.getUpdated() > 0) {
            UpdateBiographyResponse response = new UpdateBiographyResponse();

            response.setUpdatedAt(updateResult.getUpdatedAt());

            return ResponseEntity.ok(response);
        }
        Biography biography = biographyService.getBiographyById(timeZone, id);

        if (biography == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(modelMapper.convertToBiographyResponse(biography));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "")
    public ResponseEntity<?> create(
            @Valid @RequestBody BiographyRequest biographyRequest,
            BindingResult bindingResult
    ) throws SQLException {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        biographyService.create(biographyRequest);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("isAuthenticated() and (@biography.isIAuthor(#biographyId) or hasRole('ROLE_MODERATOR'))")
    @DeleteMapping("/{biographyId}")
    public ResponseEntity<?> delete(@PathVariable("biographyId") int biographyId) {
        int deleted = biographyService.delete(biographyId);

        if (deleted == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{biographyId}/comments")
    public ResponseEntity<Page<BiographyCommentResponse>> getComments(
            TimeZone timeZone,
            @PathVariable("biographyId") Integer biographyId,
            OffsetLimitPageRequest pageRequest
    ) {
        Page<BiographyComment> page = biographyCommentService.getComments(timeZone, biographyId, pageRequest);

        if (page.getContent().isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        return ResponseEntity.ok(
                new PageImpl<>(
                        modelMapper.convertToBiographyCommentResponse(page.getContent()),
                        pageRequest,
                        page.getTotalElements()
                )
        );
    }

    @PreAuthorize("isAuthenticated() and @biography.isCommentsEnabled(biographyId)")
    @PostMapping("/{biographyId}/comments")
    public ResponseEntity<BiographyCommentResponse> addComment(
            TimeZone timeZone,
            @PathVariable("biographyId") Integer biographyId,
            @RequestBody BiographyCommentRequest commentRequest,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        BiographyComment biographyComment = biographyCommentService.addComment(timeZone, biographyId, commentRequest);

        return ResponseEntity.ok(modelMapper.convertToBiographyCommentResponse(biographyComment));
    }

    @PreAuthorize("isAuthenticated() and (@comment.isIAuthor(#commentId) or @biography.isIAuthor(#biographyId) or hasRole('ROLE_MODERATOR'))")
    @DeleteMapping("/{biographyId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable("commentId") Integer commentId,
            @PathVariable("biographyId") Integer biographyId
    ) {
        biographyCommentService.deleteComment(commentId);

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated() and (@biography.isIAuthor(#biographyId) or hasRole('ROLE_MODERATOR'))")
    @PostMapping("/{biographyId}/publish")
    public ResponseEntity<?> publish(TimeZone timeZone, @PathVariable("biographyId") Integer biographyId) {
        int updated = biographyService.publish(timeZone, biographyId);

        if (updated == 0) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated() and (@biography.isIAuthor(#biographyId) or hasRole('ROLE_MODERATOR'))")
    @PostMapping("/{biographyId}/unpublish")
    public ResponseEntity<?> unpublish(TimeZone timeZone, @PathVariable("biographyId") Integer biographyId) {
        int updated = biographyService.unpublish(timeZone, biographyId);

        if (updated == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{biographyId}/fixes")
    public ResponseEntity<?> suggest(
            @PathVariable("biographyId") int biographyId,
            @RequestBody BiographyFixSuggestRequest suggestRequest
    ) {
        fixService.suggest(biographyId, suggestRequest);

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    @PatchMapping("/{biographyId}/moderation/assign-me")
    public ResponseEntity<?> assignMe(@PathVariable("biographyId") int biographyId,
                                      @RequestBody CompleteRequest completeRequest) throws SQLException {
        Handler.Signal signal = Handler.Signal.fromDesc(completeRequest.getSignal());

        if (signal == null || !signal.equals(Handler.Signal.ASSIGN_ME)) {
            return ResponseEntity.badRequest().build();
        }
        CompleteResult<Biography> updated = biographyModerationService.complete(
                biographyId,
                completeRequest
        );
        Biography biography = biographyModerationService.getModeratorInfo(biographyId);

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
    @PatchMapping("/{biographyId}/moderation/complete")
    public ResponseEntity<?> complete(
            @PathVariable("biographyId") int biographyId,
            @RequestBody CompleteRequest completeRequest
    ) throws SQLException {
        Handler.Signal signal = Handler.Signal.fromDesc(completeRequest.getSignal());

        if (signal == null) {
            return ResponseEntity.badRequest().build();
        }
        if (signal.equals(Handler.Signal.REJECT) && StringUtils.isBlank(completeRequest.getInfo())) {
            return ResponseEntity.badRequest().build();
        }
        CompleteResult<Biography> updated = biographyModerationService.complete(
                biographyId,
                completeRequest
        );

        if (updated.getUpdated() == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(modelMapper.convertToBiographyModerationResponse(updated.getObject()));
    }

    @PreAuthorize("isAuthenticated() and @biography.isIAuthor(#biographyId)")
    @PatchMapping("/{biographyId}/moderation/user-complete")
    public ResponseEntity<?> userComplete(
            @PathVariable("biographyId") int biographyId,
            @RequestBody CompleteRequest completeRequest
    ) throws SQLException {
        CompleteResult<Biography> updated = biographyModerationService.userComplete(
                biographyId,
                completeRequest
        );

        if (updated.getUpdated() == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(modelMapper.convertToMyBiographyResponse(updated.getObject()));
    }

    @PreAuthorize("hasRole('ROLE_MODERATOR')")
    @GetMapping(value = "/moderation")
    public ResponseEntity<Page<BiographyModerationResponse>> getModeration(
            TimeZone timeZone,
            OffsetLimitPageRequest pageRequest,
            @RequestParam(value = "q", required = false) String query
    ) throws ScriptException, NoSuchMethodException {
        Page<Biography> page = biographyModerationService.getBiographies(timeZone, pageRequest, query, null);

        if (page.getContent().size() == 0) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(
                new PageImpl<>(
                        modelMapper.convertToBiographyModerationResponse(page.getContent()),
                        page.getPageable(),
                        page.getTotalElements()
                )
        );
    }

    @PreAuthorize("isAuthenticated() and (@biography.isIAuthor(#biographyId) or hasRole('ROLE_MODERATOR'))")
    @RequestMapping(value = "/{biographyId}", method = RequestMethod.HEAD)
    public ResponseEntity<?> canEdit(@PathVariable("biographyId") int biographyId) {
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("isAuthenticated() and (@biography.isIAuthor(#biographyId) or hasRole('ROLE_MODERATOR'))")
    @PatchMapping("/{biographyId}")
    public ResponseEntity<?> update(@PathVariable("biographyId") int biographyId, @RequestBody Map<String, Object> values) {
        int updated = biographyService.partialUpdate(biographyId, values);

        if (updated == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }
}
