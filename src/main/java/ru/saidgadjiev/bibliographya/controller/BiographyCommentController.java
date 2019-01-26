package ru.saidgadjiev.bibliographya.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliographya.service.impl.BiographyCommentService;

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
    public ResponseEntity<?> update(@PathVariable("id") Integer commentId,
                                    @RequestBody ObjectNode comment) {
        int result = biographyCommentService.updateComment(commentId, comment.get("content").asText());

        if (result == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }
}
