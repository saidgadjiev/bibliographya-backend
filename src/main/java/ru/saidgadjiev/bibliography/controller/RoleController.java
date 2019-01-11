package ru.saidgadjiev.bibliography.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliography.domain.Role;
import ru.saidgadjiev.bibliography.service.impl.RoleService;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("")
    public ResponseEntity<?> get() {
        return ResponseEntity.ok(roleService.getRoles().stream().map(Role::getName).collect(Collectors.toList()));
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
