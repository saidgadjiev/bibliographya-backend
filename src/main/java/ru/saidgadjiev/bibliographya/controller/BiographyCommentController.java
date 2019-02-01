package ru.saidgadjiev.bibliographya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliographya.model.BiographyCommentRequest;
import ru.saidgadjiev.bibliographya.service.impl.BiographyCommentService;

import javax.validation.Valid;

/**
 * Created by said on 16.11.2018.
 */
@RestController
@RequestMapping("/api/comments")
public class BiographyCommentController {

    private BiographyCommentService biographyCommentService;

    @Autowired
    public BiographyCommentController(BiographyCommentService biographyCommentService) {
        this.biographyCommentService = biographyCommentService;
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable("id") Integer commentId,
            @Valid @RequestBody BiographyCommentRequest commentRequest,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        int result = biographyCommentService.updateComment(commentId, commentRequest);

        if (result == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }
}
