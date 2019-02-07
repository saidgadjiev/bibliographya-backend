package ru.saidgadjiev.bibliographya.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.saidgadjiev.bibliographya.service.impl.StatsService;

@RestController
@RequestMapping("/api/stats")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("")
    public ResponseEntity<?> stats() {
        return ResponseEntity.ok(statsService.getStats());
    }
}
