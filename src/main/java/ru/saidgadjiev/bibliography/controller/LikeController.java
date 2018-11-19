package ru.saidgadjiev.bibliography.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliography.service.impl.BiographyLikeService;

/**
 * Created by said on 15.11.2018.
 */
@RestController
@RequestMapping("api/like")
@PreAuthorize("isAuthenticated()")
public class LikeController {

    private BiographyLikeService likeService;

    @Autowired
    public LikeController(BiographyLikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> like(@PathVariable("id") int biographyId) {
        likeService.like(biographyId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}/unlike")
    public ResponseEntity<?> unlike(@PathVariable("id") int biographyId) {
        likeService.unlike(biographyId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
