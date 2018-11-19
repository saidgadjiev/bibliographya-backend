package ru.saidgadjiev.bibliography.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliography.domain.BiographyComment;
import ru.saidgadjiev.bibliography.model.BiographyCommentRequest;
import ru.saidgadjiev.bibliography.model.BiographyCommentResponse;
import ru.saidgadjiev.bibliography.service.impl.BiographyCommentService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by said on 16.11.2018.
 */
@RestController
@RequestMapping("/api/comment/")
public class BiographyCommentController {

    private BiographyCommentService biographyCommentService;

    private final ModelMapper modelMapper;

    @Autowired
    public BiographyCommentController(BiographyCommentService biographyCommentService, ModelMapper modelMapper) {
        this.biographyCommentService = biographyCommentService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("{id}")
    public ResponseEntity<Page<BiographyCommentResponse>> getComments(
            @PathVariable("id") Integer biographyId,
            @PageableDefault(page = 0, size = 10, sort = "firstName", direction = Sort.Direction.DESC) Pageable pageRequest
    ) {
        Page<BiographyComment> page = biographyCommentService.getComments(biographyId, pageRequest);

        if (page.getContent().isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        return ResponseEntity.ok(new PageImpl<>(convertToDto(page.getContent()), pageRequest, page.getTotalElements()));
    }

    @PostMapping("{id}/add")
    public ResponseEntity<?> addComment(@PathVariable("id") Integer biographyId, BiographyCommentRequest commentRequest) {
        biographyCommentService.addComment(biographyId, commentRequest);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{id}/delete")
    public ResponseEntity<?> deleteComment(@PathVariable("id") Integer biographyId) {
        biographyCommentService.deleteComment(biographyId);

        return ResponseEntity.ok().build();
    }

    private List<BiographyCommentResponse> convertToDto(List<BiographyComment> biographyComments) {
        List<BiographyCommentResponse> biographyCommentResponses = new ArrayList<>();

        for (BiographyComment biographyComment: biographyComments) {
            BiographyCommentResponse biographyCommentResponse = modelMapper.map(biographyComment, BiographyCommentResponse.class);

            biographyCommentResponse.setFirstName(biographyComment.getBiography().getFirstName());
            biographyCommentResponse.setLastName(biographyComment.getBiography().getLastName());
            biographyCommentResponse.setUserName(biographyComment.getUserName());

            if (biographyComment.getParent() != null) {
                biographyCommentResponse.setReplyToFirstName(biographyComment.getParent().getBiography().getFirstName());
                biographyCommentResponse.setReplyToUserName(biographyComment.getParent().getBiography().getUserName());
                biographyCommentResponse.setReply(true);
            }

            biographyCommentResponses.add(biographyCommentResponse);
        }

        return biographyCommentResponses;
    }
}
