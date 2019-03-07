package ru.saidgadjiev.bibliographya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliographya.domain.SaveEmail;
import ru.saidgadjiev.bibliographya.model.GeneralSettings;
import ru.saidgadjiev.bibliographya.model.RestorePassword;
import ru.saidgadjiev.bibliographya.model.SavePassword;
import ru.saidgadjiev.bibliographya.service.api.BibliographyaUserDetailsService;
import ru.saidgadjiev.bibliographya.service.impl.SettingsService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Locale;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private SettingsService settingsService;

    private BibliographyaUserDetailsService userDetailsService;

    @Autowired
    public SettingsController(SettingsService settingsService, BibliographyaUserDetailsService userDetailsService) {
        this.settingsService = settingsService;
        this.userDetailsService = userDetailsService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/general")
    public ResponseEntity<?> getSettings() {
        GeneralSettings generalSettings = settingsService.getGeneralSettings();

        return ResponseEntity.ok(generalSettings);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/general/email")
    public ResponseEntity<?> getEmail() {
        GeneralSettings generalSettings = settingsService.getGeneralSettings();

        return ResponseEntity.ok(generalSettings);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/save-email/finish")
    public ResponseEntity<?> saveEmail(HttpServletRequest request, @Valid @RequestBody SaveEmail saveEmail, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        HttpStatus status = userDetailsService.saveEmailFinish(request, saveEmail);

        return ResponseEntity.status(status).build();
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/save-email/start")
    public ResponseEntity<?> changeEmail(HttpServletRequest request, Locale locale, @RequestParam("email") String email) {
        HttpStatus status = userDetailsService.saveEmailStart(request, locale, email);

        return ResponseEntity.status(status).build();
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/save-password")
    public ResponseEntity<?> savePassword(@Valid @RequestBody SavePassword savePassword, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        HttpStatus changeStatus = userDetailsService.savePassword(savePassword);

        return ResponseEntity.status(changeStatus).build();
    }

    @PostMapping("/restore-password/start")
    public ResponseEntity<?> restorePassword(HttpServletRequest request, Locale locale, @RequestParam("email") String email) {
        HttpStatus restoreResult = userDetailsService.restorePasswordStart(request, locale, email);

        return ResponseEntity.status(restoreResult).build();
    }

    @PostMapping("/restore-password/finish")
    public ResponseEntity<?> changePassword(HttpServletRequest request,
                                            @RequestBody RestorePassword restorePassword,
                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        HttpStatus restoreStatus = userDetailsService.restorePasswordFinish(request, restorePassword);

        return ResponseEntity.status(restoreStatus).build();
    }
}
