package ru.saidgadjiev.bibliographya.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliographya.domain.AuthenticationKey;
import ru.saidgadjiev.bibliographya.domain.AuthenticationKeyConfirmation;
import ru.saidgadjiev.bibliographya.domain.SendVerificationResult;
import ru.saidgadjiev.bibliographya.model.GeneralSettings;
import ru.saidgadjiev.bibliographya.model.RestorePassword;
import ru.saidgadjiev.bibliographya.model.SavePassword;
import ru.saidgadjiev.bibliographya.service.api.BibliographyaUserDetailsService;
import ru.saidgadjiev.bibliographya.service.impl.SettingsService;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Locale;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private ObjectMapper objectMapper;

    private SettingsService settingsService;

    private BibliographyaUserDetailsService userDetailsService;

    @Autowired
    public SettingsController(ObjectMapper objectMapper,
                              SettingsService settingsService,
                              BibliographyaUserDetailsService userDetailsService) {
        this.objectMapper = objectMapper;
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
    public ResponseEntity<?> saveEmail(HttpServletRequest request,
                                       @Valid @RequestBody AuthenticationKeyConfirmation authenticationKeyConfirmation,
                                       BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }
        if (!authenticationKeyConfirmation.getAuthenticationKey().getType().equals(AuthenticationKey.Type.EMAIL)) {
            return ResponseEntity.badRequest().build();
        }

        HttpStatus status = userDetailsService.saveEmailFinish(request, authenticationKeyConfirmation);

        return ResponseEntity.status(status).build();
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/save-email/start")
    public ResponseEntity<?> changeEmail(HttpServletRequest request,
                                         Locale locale,
                                         AuthenticationKey authenticationKey) throws MessagingException {
        if (!authenticationKey.getType().equals(AuthenticationKey.Type.EMAIL)) {
            return ResponseEntity.badRequest().build();
        }

        SendVerificationResult sendVerificationResult = userDetailsService.saveEmailStart(request, locale, authenticationKey);

        return ResponseEntity.status(sendVerificationResult.getStatus()).body(sendVerificationResult.getTimer());
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
    public ResponseEntity<?> restorePassword(HttpServletRequest request,
                                             Locale locale,
                                             AuthenticationKey authenticationKey) throws MessagingException {
        SendVerificationResult sendVerificationResult = userDetailsService.restorePasswordStart(request, locale, authenticationKey);

        return ResponseEntity.status(sendVerificationResult.getStatus()).body(sendVerificationResult.getTimer());
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

    @PostMapping("/save-phone/start")
    public ResponseEntity<?> savePhoneStart(HttpServletRequest request, Locale locale, AuthenticationKey authenticationKey) throws MessagingException {
        SendVerificationResult sendVerificationResult = userDetailsService.savePhoneStart(request, locale, authenticationKey);

        return ResponseEntity.status(sendVerificationResult.getStatus()).body(sendVerificationResult.getTimer());
    }

    @PostMapping("/save-phone/finish")
    public ResponseEntity<?> savePhoneFinish(HttpServletRequest request,
                                             @Valid @RequestBody AuthenticationKeyConfirmation authenticationKeyConfirmation,
                                             BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        HttpStatus status = userDetailsService.savePhoneFinish(request, authenticationKeyConfirmation);

        return ResponseEntity.status(status).build();
    }
}
