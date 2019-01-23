package ru.saidgadjiev.bibliography.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.saidgadjiev.bibliography.service.api.UserAccountDetailsService;

import java.sql.SQLException;

/**
 * Created by said on 02.01.2019.
 */
//@RestController
//@RequestMapping("/api/user-accounts")
public class UserAccountController {

    private final UserAccountDetailsService userAccountDetailsService;

    public UserAccountController(UserAccountDetailsService userAccountDetailsService) {
        this.userAccountDetailsService = userAccountDetailsService;
    }

    @RequestMapping(value = "/{username}", method = RequestMethod.HEAD)
    public ResponseEntity existUserName(@PathVariable(value = "username") String username) throws SQLException {
        if (userAccountDetailsService.isExistUserName(username)) {
            return ResponseEntity.status(HttpStatus.FOUND).build();
        }

        return ResponseEntity.ok().build();
    }
}
