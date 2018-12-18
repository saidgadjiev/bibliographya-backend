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
import ru.saidgadjiev.bibliography.service.impl.BiographyService;
import ru.saidgadjiev.bibliography.service.impl.moderation.BiographyModerationService;

import javax.validation.Valid;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by said on 22.10.2018.
 */

@RestController
@RequestMapping("/api/biography")
public class BiographyController {

    private BiographyService biographyService;

    private BiographyModerationService biographyModerationService;

    private final ModelMapper modelMapper;

    @Autowired
    public BiographyController(BiographyService biographyService,
                               BiographyModerationService biographyModerationService,
                               ModelMapper modelMapper) {
        this.biographyService = biographyService;
        this.biographyModerationService = biographyModerationService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("")
    public ResponseEntity<BiographyResponse> getBiography(
            @PathVariable(value = "username", required = false) String userNameFilter
    ) throws SQLException {
        return ResponseEntity.ok(convertToDto(biographyService.getBiography(userNameFilter)));
    }

    @GetMapping("/{id:[\\\\d]+}")
    public ResponseEntity<BiographyResponse> getBiographyById(@PathVariable("id") int id) throws SQLException {
        return ResponseEntity.ok(convertToDto(biographyService.getBiographyById(id)));
    }

    //Filter eq: creatorName=eq:1
    @GetMapping(value = "/biographies/{categoryName}")
    public ResponseEntity<Page<BiographyResponse>> getBiographies(
            OffsetLimitPageRequest pageRequest,
            @PathVariable(value = "categoryName") String categoryName
    ) throws SQLException {
        Page<Biography> page = biographyService.getBiographies(
                pageRequest, null, null, null, categoryName
        );

        if (page.getContent().size() == 0) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(new PageImpl<>(convertToDto(page.getContent()), page.getPageable(), page.getTotalElements()));
    }

    @GetMapping(value = "/my/biographies")
    public ResponseEntity<Page<BiographyResponse>> getBiographies(
            OffsetLimitPageRequest pageRequest
    ) throws SQLException {
        Page<Biography> page = biographyService.getMyBiographies(pageRequest);

        if (page.getContent().size() == 0) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(new PageImpl<>(convertToDto(page.getContent()), page.getPageable(), page.getTotalElements()));
    }

    @PutMapping(value = "/update/{id}")
    public ResponseEntity<?> update(
            @PathVariable("id") Integer id,
            @Valid @RequestBody BiographyRequest biographyRequest,
            BindingResult bindingResult
    ) throws SQLException {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        BiographyUpdateStatus updateResult = biographyService.update(id, biographyRequest);

        if (updateResult.isUpdated()) {
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

    @PostMapping(value = "/create")
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

    private List<BiographyResponse> convertToDto(List<Biography> biographies) {
        List<BiographyResponse> dto = new ArrayList<>();

        for (Biography biography : biographies) {
            BiographyResponse biographyResponse = modelMapper.map(biography, BiographyResponse.class);

            biographyResponse.setActions(biographyModerationService.getUserActions(biography));

            dto.add(biographyResponse);
        }

        return dto;
    }

    private BiographyResponse convertToDto(Biography biography) {
        return modelMapper.map(biography, BiographyResponse.class);
    }
}
