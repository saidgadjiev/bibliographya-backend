package ru.saidgadjiev.bibliography.controller;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliography.domain.User;
import ru.saidgadjiev.bibliography.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliography.model.UserResponse;
import ru.saidgadjiev.bibliography.service.impl.UserService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    private final ModelMapper modelMapper;

    public UserController(UserService userService, ModelMapper modelMapper) {
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

        return ResponseEntity.ok(new PageImpl<>(convertToDto(users.getContent())));
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

    private List<UserResponse> convertToDto(Collection<User> users) {
        List<UserResponse> result = new ArrayList<>();

        for (User user: users) {
            result.add(modelMapper.map(user, UserResponse.class));
        }

        return result;
    }
}
