package ru.saidgadjiev.bibliography.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliography.domain.User;
import ru.saidgadjiev.bibliography.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliography.model.UserResponse;
import ru.saidgadjiev.bibliography.service.impl.UserService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("")
    public ResponseEntity<?> getUsers(OffsetLimitPageRequest pageRequest) {
        Page<User> users = userService.getUsers(pageRequest);

        if (users.getContent().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(new PageImpl<>(convertToDto(users.getContent())));
    }

    @PostMapping("/{userId}/roles/{role}")
    public ResponseEntity<?> addRole(@PathVariable("userId") int userId, @PathVariable("role") String role) {
        userService.addRole(userId, role);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/roles/{role}")
    public ResponseEntity<?> getRole(@PathVariable("userId") int userId, @PathVariable("role") String role) {
        userService.deleteRole(userId, role);

        return ResponseEntity.ok().build();
    }

    private List<UserResponse> convertToDto(Collection<User> users) {
        return Collections.emptyList();
    }
}
