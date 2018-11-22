package ru.saidgadjiev.bibliography.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.BiographyUpdateStatus;
import ru.saidgadjiev.bibliography.model.*;
import ru.saidgadjiev.bibliography.service.impl.BiographyCommentService;
import ru.saidgadjiev.bibliography.service.impl.BiographyLikeService;
import ru.saidgadjiev.bibliography.service.impl.BiographyService;

import javax.validation.Valid;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by said on 22.10.2018.
 */

@RestController
@RequestMapping("/api/biography")
public class BiographyController {

    private final BiographyService biographyService;

    private final BiographyLikeService biographyLikeService;

    private final BiographyCommentService biographyCommentService;

    private final ModelMapper modelMapper;

    @Autowired
    public BiographyController(BiographyService biographyService,
                               BiographyLikeService biographyLikeService,
                               BiographyCommentService biographyCommentService,
                               ModelMapper modelMapper) {
        this.biographyService = biographyService;
        this.biographyLikeService = biographyLikeService;
        this.biographyCommentService = biographyCommentService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<BiographyResponse> getBiographyByUsername(@PathVariable("username") String username) {
        return ResponseEntity.ok(convertToDto(biographyService.getBiographyByUsername(username)));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<BiographyResponse> getBiographyById(@PathVariable("id") int id) throws SQLException {
        return ResponseEntity.ok(convertToDto(biographyService.getBiographyById(id)));
    }

    @GetMapping(value = "")
    public ResponseEntity<Page<BiographyResponse>> getBiographies(
            OffsetLimitPageRequest pageRequest
    ) throws SQLException {
        Page<Biography> page = biographyService.getBiographies(pageRequest);

        if (page.getContent().size() == 0) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(new PageImpl<>(convertToDto(page.getContent()), page.getPageable(), page.getTotalElements()));
    }

    @PutMapping(value = "/update/{id}")
    public ResponseEntity<?> update(
            @PathVariable("id") Integer id,
            @Valid @RequestBody UpdateBiographyRequest updateBiographyRequest,
            BindingResult bindingResult
    ) throws SQLException {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        BiographyUpdateStatus updateResult = biographyService.update(id, updateBiographyRequest);

        if (updateResult.isUpdated()) {
            UpdateBiographyResponse response = new UpdateBiographyResponse();

            response.setLastModified(new LastModified(
                    updateResult.getUpdatedAt().getTime(), updateResult.getUpdatedAt().getNanos())
            );

            return ResponseEntity.ok(response);
        }
        Biography biography = biographyService.getBiographyById(id);

        if (biography == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(convertToDto(biography));
    }

    private List<BiographyResponse> convertToDto(List<Biography> biographies) {
        List<BiographyResponse> dto = new ArrayList<>();
        Collection<Integer> ids = biographies.stream().map(Biography::getId).collect(Collectors.toList());
        Map<Integer, Integer> biographiesLikesCount = biographyLikeService.getBiographiesLikesCount(ids);
        Map<Integer, Boolean> biographiesIsLiked = biographyLikeService.getBiographiesIsLiked(ids);
        Map<Integer, Long> biographiesCommentsCount = biographyCommentService.getBiographiesCommentsCount(ids);

        for (Biography biography : biographies) {
            BiographyResponse biographyResponse = modelMapper.map(biography, BiographyResponse.class);

            biographyResponse.setLikesCount(biographiesLikesCount.get(biography.getId()));
            biographyResponse.setLiked(biographiesIsLiked.get(biography.getId()));
            biographyResponse.setCommentsCount(biographiesCommentsCount.get(biography.getId()));
            dto.add(biographyResponse);
        }

        return dto;
    }

    private BiographyResponse convertToDto(Biography biography) {
        BiographyResponse biographyResponse = modelMapper.map(biography, BiographyResponse.class);

        biographyResponse.setLikesCount(biographyLikeService.getBiographyLikesCount(biography.getId()));
        biographyResponse.setCommentsCount(biographyCommentService.getBiographyCommentsCount(biography.getId()));
        biographyResponse.setLiked(biographyLikeService.getBiographyIsLiked(biography.getId()));

        return biographyResponse;
    }
}
