package ru.saidgadjiev.bibliography.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliography.service.impl.BiographyLikeService;
import ru.saidgadjiev.bibliography.service.impl.LikesPusherService;

/**
 * Created by said on 15.11.2018.
 */
@RestController
@RequestMapping("api/biographies/{biographyId}/likes")
@PreAuthorize("isAuthenticated()")
public class LikeController {

    private BiographyLikeService likeService;

    private LikesPusherService likesPusherService;

    @Autowired
    public LikeController(BiographyLikeService likeService, LikesPusherService likesPusherService) {
        this.likeService = likeService;
        this.likesPusherService = likesPusherService;
    }

    @PostMapping("")
    public ResponseEntity<?> like(
            @PathVariable("biographyId") int biographyId,
            @RequestParam("socketId") String socketId
    ) {
        likeService.like(biographyId);

        likesPusherService.addLike(biographyId, socketId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("")
    public ResponseEntity<?> unlike(
            @PathVariable("biographyId") int biographyId,
            @RequestParam("socketId") String socketId
    ) {
        likeService.unlike(biographyId);

        likesPusherService.deleteLike(biographyId, socketId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
