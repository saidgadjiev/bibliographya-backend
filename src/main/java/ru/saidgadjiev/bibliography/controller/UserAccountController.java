package ru.saidgadjiev.bibliography.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.saidgadjiev.bibliography.service.api.UserService;

import java.sql.SQLException;

/**
 * Created by said on 02.01.2019.
 */
@RestController
@RequestMapping("/api/users")
public class UserAccountController {

    private final UserService userService;

    public UserAccountController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "/{username}", method = RequestMethod.HEAD)
    public ResponseEntity existUserName(@PathVariable(value = "username") String username) throws SQLException {
        if (userService.isExistUserName(username)) {
            return ResponseEntity.status(HttpStatus.FOUND).build();
        }

        return ResponseEntity.ok().build();
    }
}
