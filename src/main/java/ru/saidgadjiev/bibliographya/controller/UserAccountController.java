package ru.saidgadjiev.bibliographya.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliographya.domain.SaveEmail;
import ru.saidgadjiev.bibliographya.model.RestorePassword;
import ru.saidgadjiev.bibliographya.model.SavePassword;
import ru.saidgadjiev.bibliographya.service.api.BibliographyaUserDetailsService;
import ru.saidgadjiev.bibliographya.utils.ResponseUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.sql.SQLException;
import java.util.Locale;

/**
 * Created by said on 02.01.2019.
 */
@RestController
@RequestMapping("/api/user-accounts")
public class UserAccountController {

    private final BibliographyaUserDetailsService userAccountDetailsService;

    public UserAccountController(BibliographyaUserDetailsService userAccountDetailsService) {
        this.userAccountDetailsService = userAccountDetailsService;
    }

    @RequestMapping(value = "/{email}", method = RequestMethod.HEAD)
    public ResponseEntity checkEmail(@PathVariable(value = "email") String email) throws SQLException {
        if (userAccountDetailsService.isExistEmail(email)) {
            return ResponseEntity.status(HttpStatus.FOUND).build();
        }

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/save-password")
    public ResponseEntity<?> savePassword(@Valid @RequestBody SavePassword savePassword, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        HttpStatus changeStatus = userAccountDetailsService.savePassword(savePassword);

        return ResponseEntity.status(changeStatus).build();
    }

    @PostMapping("/restore-password")
    public ResponseEntity<?> restorePassword(HttpServletRequest request, Locale locale, @RequestParam("email") String email) {
        HttpStatus restoreResult = userAccountDetailsService.restorePassword(request, locale, email);

        return ResponseEntity.status(restoreResult).build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(HttpServletRequest request, @RequestBody RestorePassword restorePassword, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        HttpStatus restoreStatus = userAccountDetailsService.restorePassword(request, restorePassword);

        return ResponseEntity.status(restoreStatus).build();
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/save-email")
    public ResponseEntity<?> saveEmail(HttpServletRequest request, @Valid @RequestBody SaveEmail saveEmail, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        HttpStatus status = userAccountDetailsService.saveEmail(request, saveEmail);

        return ResponseEntity.status(status).build();
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/change-email")
    public ResponseEntity<?> changeEmail(HttpServletRequest request, Locale locale, @RequestParam("newEmail") String newEmail) {
        HttpStatus status = userAccountDetailsService.changeEmail(request, locale, newEmail);

        return ResponseEntity.status(status).build();
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("email") String email, @RequestParam("code") Integer code) {
        userAccountDetailsService.verifyEmail(email, code);
    }
}
