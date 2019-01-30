package ru.saidgadjiev.bibliographya.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliographya.data.mapper.BibliographyaMapper;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliographya.service.impl.UserService;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class UserController {

    private final UserService userService;

    private final BibliographyaMapper modelMapper;

    public UserController(UserService userService, BibliographyaMapper modelMapper) {
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("")
    public ResponseEntity<?> getUsers(
            OffsetLimitPageRequest pageRequest,
            @RequestParam(value = "roleQuery", required = false) String roleQuery
    ) {
        Page<User> users = userService.getUsers(pageRequest, roleQuery);

        if (users.getContent().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(new PageImpl<>(modelMapper.convertToUserResponse(users.getContent())));
    }

    @PostMapping("/{userId}/roles/{role}")
    public ResponseEntity<?> addRole(@PathVariable("userId") int userId, @PathVariable("role") String role) {
        userService.addRole(userId, role);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/roles/{role}")
    public ResponseEntity<?> getRole(@PathVariable("userId") int userId, @PathVariable("role") String role) {
        int deleted = userService.deleteRole(userId, role);

        if (deleted == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }
}
