package ru.saidgadjiev.bibliographya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliographya.data.mapper.BibliographyaMapper;
import ru.saidgadjiev.bibliographya.domain.BiographyLike;
import ru.saidgadjiev.bibliographya.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliographya.service.impl.BiographyLikeService;

/**
 * Created by said on 15.11.2018.
 */
@RestController
@RequestMapping("api/biographies/{biographyId}/likes")
@PreAuthorize("isAuthenticated()")
public class LikeController {

    private BiographyLikeService likeService;

    private final BibliographyaMapper bibliographyaMapper;

    @Autowired
    public LikeController(BiographyLikeService likeService, BibliographyaMapper bibliographyaMapper) {
        this.likeService = likeService;
        this.bibliographyaMapper = bibliographyaMapper;
    }

    @PostMapping("")
    public ResponseEntity<?> like(
            @PathVariable("biographyId") int biographyId
    ) {
        likeService.like(biographyId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("")
    public ResponseEntity<?> unlike(
            @PathVariable("biographyId") int biographyId
    ) {
        likeService.unlike(biographyId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("")
    public ResponseEntity<?> likes(@PathVariable("biographyId") int biographyId, OffsetLimitPageRequest pageRequest) {
        Page<BiographyLike> page = likeService.getBiographyLikes(biographyId, pageRequest);

        if (page.getContent().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(
                new PageImpl<>(
                        bibliographyaMapper.convertToShortBiographyResponse(page.getContent()),
                        pageRequest,
                        page.getTotalElements()
                )
        );
    }
}
