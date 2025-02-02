package ru.saidgadjiev.bibliographya.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliographya.domain.Role;
import ru.saidgadjiev.bibliographya.service.impl.RoleService;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("")
    public ResponseEntity<?> get(
            @RequestParam(value = "q", required = false) String query
    ) {
        return ResponseEntity.ok(roleService.getRoles(query).stream().map(Role::getName).collect(Collectors.toList()));
    }

    @PostMapping("/{role}")
    public ResponseEntity<?> create(@PathVariable("role") String role) {
        roleService.createRole(role);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{role}")
    public ResponseEntity<?> delete(@PathVariable("role") String role) {
        roleService.deleteRole(role);

        return ResponseEntity.ok().build();
    }
}
