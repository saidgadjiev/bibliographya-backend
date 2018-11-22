package ru.saidgadjiev.bibliography.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliography.domain.BiographyComment;
import ru.saidgadjiev.bibliography.model.BiographyCommentRequest;
import ru.saidgadjiev.bibliography.model.BiographyCommentResponse;
import ru.saidgadjiev.bibliography.model.OffsetLimitPageRequest;
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
            OffsetLimitPageRequest pageRequest
    ) {
        Page<BiographyComment> page = biographyCommentService.getComments(biographyId, pageRequest);

        if (page.getContent().isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        return ResponseEntity.ok(new PageImpl<>(convertToDto(page.getContent()), pageRequest, page.getTotalElements()));
    }

    @PostMapping("{id}/add")
    public ResponseEntity<BiographyCommentResponse> addComment(@PathVariable("id") Integer biographyId,
                                                               @RequestBody BiographyCommentRequest commentRequest) {
        BiographyComment biographyComment = biographyCommentService.addComment(biographyId, commentRequest);

        return ResponseEntity.ok(convertToDto(biographyComment));
    }

    @PostMapping("update/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Integer commentId, @RequestBody ObjectNode comment) {
        int result = biographyCommentService.updateComment(commentId, comment.get("content").asText());

        if (result == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable("id") Integer commentId) {
        biographyCommentService.deleteComment(commentId);

        return ResponseEntity.ok().build();
    }

    private List<BiographyCommentResponse> convertToDto(List<BiographyComment> biographyComments) {
        List<BiographyCommentResponse> biographyCommentResponses = new ArrayList<>();

        for (BiographyComment biographyComment : biographyComments) {
            BiographyCommentResponse biographyCommentResponse = modelMapper.map(biographyComment, BiographyCommentResponse.class);

            biographyCommentResponses.add(biographyCommentResponse);
        }

        return biographyCommentResponses;
    }

    private BiographyCommentResponse convertToDto(BiographyComment biographyComment) {
        return modelMapper.map(biographyComment, BiographyCommentResponse.class);
    }
}
