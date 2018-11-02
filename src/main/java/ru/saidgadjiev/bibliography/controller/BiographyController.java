package ru.saidgadjiev.bibliography.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.service.impl.BiographyService;

import java.sql.SQLException;

/**
 * Created by said on 22.10.2018.
 */

@RestController
@RequestMapping("/api/biography")
public class BiographyController {

    private final BiographyService biographyService;

    @Autowired
    public BiographyController(BiographyService biographyService) {
        this.biographyService = biographyService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<Biography> getUsernameBiography(@PathVariable("username") String username) {
        return ResponseEntity.ok(biographyService.getUsernameBiography(username));
    }

    @GetMapping(value = "")
    public ResponseEntity<Page<Biography>> getBiographies(
            @PageableDefault(page = 0, size = 10, sort = "firstName", direction = Sort.Direction.DESC) Pageable pageRequest
    ) throws SQLException {
        Page<Biography> page = biographyService.getBiographies(pageRequest);

        if (page.getContent().size() == 0) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(page);
    }
}
