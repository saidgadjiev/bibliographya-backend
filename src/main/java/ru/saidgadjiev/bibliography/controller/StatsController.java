package ru.saidgadjiev.bibliography.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.saidgadjiev.bibliography.service.impl.UserService;

@RestController
@RequestMapping("/api/stats")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class StatsController {

    private final UserService userService;

    public StatsController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("")
    public ResponseEntity<?> stats() {
        return ResponseEntity.ok(userService.getStats());
    }
}
