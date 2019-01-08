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
@RequestMapping("/api/comments")
public class BiographyCommentController {

    private BiographyCommentService biographyCommentService;

    @Autowired
    public BiographyCommentController(BiographyCommentService biographyCommentService) {
        this.biographyCommentService = biographyCommentService;
    }

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
