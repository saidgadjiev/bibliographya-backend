package ru.saidgadjiev.bibliography.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliography.bussiness.moderation.ModerationAction;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.BiographyComment;
import ru.saidgadjiev.bibliography.domain.BiographyUpdateStatus;
import ru.saidgadjiev.bibliography.domain.CompleteResult;
import ru.saidgadjiev.bibliography.model.*;
import ru.saidgadjiev.bibliography.service.impl.BiographyCommentService;
import ru.saidgadjiev.bibliography.service.impl.BiographyFixService;
import ru.saidgadjiev.bibliography.service.impl.BiographyModerationService;
import ru.saidgadjiev.bibliography.service.impl.BiographyService;

import javax.validation.Valid;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

    private final ModelMapper modelMapper;

    @Autowired
    public BiographyController(BiographyService biographyService,
                               BiographyModerationService biographyModerationService,
                               BiographyCommentService biographyCommentService,
                               BiographyFixService fixService,
                               ModelMapper modelMapper
    ) {
        this.biographyService = biographyService;
        this.biographyModerationService = biographyModerationService;
        this.biographyCommentService = biographyCommentService;
        this.fixService = fixService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/{id:[\\d]+}")
    public ResponseEntity<BiographyResponse> getBiographyById(@PathVariable("id") int id) throws SQLException {
        return ResponseEntity.ok(convertToDto(biographyService.getBiographyById(id)));
    }

    @GetMapping(value = "")
    public ResponseEntity<Page<BiographyResponse>> getBiographies(
            OffsetLimitPageRequest pageRequest,
            @RequestParam(value = "autobiographies", required = false) Boolean autobiographies
    ) throws SQLException {
        Page<Biography> page = biographyService.getBiographies(pageRequest, null, autobiographies);

        if (page.getContent().size() == 0) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(new PageImpl<>(convertToDto(page.getContent()), page.getPageable(), page.getTotalElements()));
    }

    @GetMapping(value = "/my")
    public ResponseEntity<Page<BiographyResponse>> getMyBiographies(
            OffsetLimitPageRequest pageRequest
    ) throws SQLException {
        Page<Biography> page = biographyService.getMyBiographies(pageRequest);

        if (page.getContent().size() == 0) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(new PageImpl<>(convertMyBiographiesToDto(page.getContent()), page.getPageable(), page.getTotalElements()));
    }

    @PutMapping(value = "/{id:[\\d]+}")
    public ResponseEntity<?> update(
            @PathVariable("id") Integer id,
            @Valid @RequestBody BiographyRequest biographyRequest,
            BindingResult bindingResult
    ) throws SQLException {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        BiographyUpdateStatus updateResult = biographyService.update(id, biographyRequest);

        if (updateResult.getUpdated() > 0) {
            UpdateBiographyResponse response = new UpdateBiographyResponse();

            response.setLastModified(
                    new LastModified(
                            updateResult.getUpdatedAt().getTime(), updateResult.getUpdatedAt().getNanos()
                    )
            );

            return ResponseEntity.ok(response);
        }
        Biography biography = biographyService.getBiographyById(id);

        if (biography == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(convertToDto(biography));
    }

    @PostMapping(value = "")
    public ResponseEntity<?> create(
            @Valid @RequestBody BiographyRequest biographyRequest,
            BindingResult bindingResult
    ) throws SQLException {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        Biography biography = biographyService.create(biographyRequest);

        BiographyResponse response = modelMapper.map(biography, BiographyResponse.class);

        response.setLiked(false);
        response.setLikesCount(0);
        response.setCommentsCount(0);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

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
            @PathVariable("biographyId") Integer biographyId,
            OffsetLimitPageRequest pageRequest
    ) {
        Page<BiographyComment> page = biographyCommentService.getComments(biographyId, pageRequest);

        if (page.getContent().isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        return ResponseEntity.ok(new PageImpl<>(convertCommentsToDto(page.getContent()), pageRequest, page.getTotalElements()));
    }

    @PostMapping("/{biographyId}/comments")
    public ResponseEntity<BiographyCommentResponse> addComment(@PathVariable("biographyId") Integer biographyId,
                                                               @RequestBody BiographyCommentRequest commentRequest) {
        BiographyComment biographyComment = biographyCommentService.addComment(biographyId, commentRequest);

        return ResponseEntity.ok(convertCommentToDto(biographyComment));
    }

    @DeleteMapping("/{biographyId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable("commentId") Integer commentId,
            @PathVariable("biographyId") Integer biographyId
    ) {
        biographyCommentService.deleteComment(biographyId, commentId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{biographyId}/publish")
    public ResponseEntity<?> publish(@PathVariable("biographyId") Integer biographyId) {
        int updated = biographyService.publish(biographyId);

        if (updated == 0) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{biographyId}/unpublish")
    public ResponseEntity<?> unpublish(@PathVariable("biographyId") Integer biographyId) {
        int updated = biographyService.unpublish(biographyId);

        if (updated == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{biographyId}/fixes")
    public ResponseEntity<?> suggest(
            @PathVariable("biographyId") int biographyId,
            @RequestBody BiographyFixSuggestRequest suggestRequest
    ) {
        fixService.suggest(biographyId, suggestRequest);

        return ResponseEntity.ok().build();
    }


    @PatchMapping("/{biographyId}/moderation/assign-me")
    public ResponseEntity<?> assignMe(@PathVariable("biographyId") int biographyId,
                                      @RequestBody CompleteRequest completeRequest) throws SQLException {
        CompleteResult<Biography, ModerationAction> updated = biographyModerationService.complete(
                biographyId,
                completeRequest
        );
        Biography biography = biographyModerationService.getModeratorInfo(biographyId);

        if (biography == null) {
            return ResponseEntity.notFound().build();
        }

        if (updated.getUpdated() == 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(convertModerationToDto(biography, Collections.emptyList()));
        }

        return ResponseEntity.ok(convertModerationToDto(biography, updated.getActions()));
    }

    @PatchMapping("/{biographyId}/moderation/complete")
    public ResponseEntity<?> complete(
            @PathVariable("biographyId") int biographyId,
            @RequestBody CompleteRequest completeRequest
    ) throws SQLException {
        CompleteResult<Biography, ModerationAction> updated = biographyModerationService.complete(
                biographyId,
                completeRequest
        );

        if (updated.getUpdated() == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(convertModerationToDto(updated.getObject(), updated.getActions()));
    }

    @PatchMapping("/{biographyId}/moderation/user-complete")
    public ResponseEntity<?> userComplete(
            @PathVariable("biographyId") int biographyId,
            @RequestBody CompleteRequest completeRequest
    ) throws SQLException {
        CompleteResult<Biography, ModerationAction> updated = biographyModerationService.userComplete(
                biographyId,
                completeRequest
        );

        if (updated.getUpdated() == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(convertModerationToDto(updated.getObject(), updated.getActions()));
    }

    @GetMapping(value = "/moderation")
    public ResponseEntity<Page<BiographyResponse>> getModeration(
            OffsetLimitPageRequest pageRequest,
            @RequestParam(value = "q", required = false) String query
    ) throws SQLException {
        Page<Biography> page = biographyModerationService.getBiographies(pageRequest, query);

        if (page.getContent().size() == 0) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(new PageImpl<>(convertModerationToDto(page.getContent()), page.getPageable(), page.getTotalElements()));
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

    private List<BiographyResponse> convertModerationToDto(List<Biography> biographies) {
        List<BiographyResponse> dto = new ArrayList<>();

        for (Biography biography : biographies) {
            BiographyResponse biographyResponse = modelMapper.map(biography, BiographyResponse.class);

            biographyResponse.setActions(biographyModerationService.getActions(biography));

            dto.add(biographyResponse);
        }

        return dto;
    }

    private BiographyResponse convertModerationToDto(Biography biography, Collection<ModerationAction> actions) {
        BiographyResponse response =  modelMapper.map(biography, BiographyResponse.class);

        response.setActions(actions);

        return response;
    }

    private List<BiographyResponse> convertMyBiographiesToDto(List<Biography> biographies) {
        List<BiographyResponse> dto = new ArrayList<>();

        for (Biography biography : biographies) {
            BiographyResponse biographyResponse = modelMapper.map(biography, BiographyResponse.class);

            biographyResponse.setActions(biographyModerationService.getUserActions(biography));

            dto.add(biographyResponse);
        }

        return dto;
    }

    private List<BiographyCommentResponse> convertCommentsToDto(List<BiographyComment> biographyComments) {
        List<BiographyCommentResponse> biographyCommentResponses = new ArrayList<>();

        for (BiographyComment biographyComment : biographyComments) {
            BiographyCommentResponse biographyCommentResponse = modelMapper.map(biographyComment, BiographyCommentResponse.class);

            biographyCommentResponses.add(biographyCommentResponse);
        }

        return biographyCommentResponses;
    }

    private BiographyCommentResponse convertCommentToDto(BiographyComment biographyComment) {
        return modelMapper.map(biographyComment, BiographyCommentResponse.class);
    }
}
