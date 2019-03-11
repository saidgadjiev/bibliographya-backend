package ru.saidgadjiev.bibliographya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliographya.auth.common.AuthContext;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.domain.RequestResult;
import ru.saidgadjiev.bibliographya.domain.SignUpConfirmation;
import ru.saidgadjiev.bibliographya.domain.SignUpResult;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.model.SignInRequest;
import ru.saidgadjiev.bibliographya.model.SignUpRequest;
import ru.saidgadjiev.bibliographya.service.impl.auth.AuthService;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.sql.SQLException;
import java.util.Locale;

/**
 * Created by said on 22.10.2018.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private AuthService authService;

    @Autowired
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/oauth/{providerId}")
    public ResponseEntity<String> signIn(@PathVariable("providerId") String providerId,
                                         @RequestParam("redirectUri") String redirectUri) {
        ProviderType providerType = ProviderType.fromId(providerId);

        if (providerType == null) {
            return ResponseEntity.badRequest().build();
        }

        if (providerType.equals(ProviderType.EMAIL_PASSWORD)) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(authService.getOauthUrl(providerType, redirectUri));
    }

    @PostMapping("/signUp/confirm-finish")
    public ResponseEntity<?> confirmSignUp(HttpServletRequest request,
                                           HttpServletResponse response,
                                           @Valid @RequestBody SignUpConfirmation signUpConfirmation,
                                           BindingResult bindingResult) throws SQLException {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        AuthContext authContext = new AuthContext()
                .setRequest(request)
                .setResponse(response)
                .setBody(signUpConfirmation);

        SignUpResult signUpResult = authService.confirmSignUpFinish(authContext);

        return ResponseEntity.status(signUpResult.getStatus()).body(signUpResult.getUser());
    }

    @PostMapping("/signUp/confirm-start")
    public ResponseEntity<?> confirmSignUp(HttpServletRequest request,
                                           Locale locale,
                                           @RequestParam("email") String email) throws MessagingException {
        HttpStatus status = authService.confirmSignUpStart(request, locale, email);

        return ResponseEntity.status(status).build();
    }

    @PostMapping("/signUp/cancel")
    public ResponseEntity<?> cancelSignUp(HttpServletRequest request) throws SQLException {
        authService.cancelSignUp(request);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/signUp")
    public ResponseEntity<?> signUp(HttpServletRequest request,
                                    @Valid @RequestBody SignUpRequest signUpRequest,
                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        AuthContext authContext = new AuthContext()
                .setProviderType(ProviderType.EMAIL_PASSWORD)
                .setRequest(request)
                .setBody(signUpRequest);

        HttpStatus status = authService.signUp(authContext, null);

        return ResponseEntity.status(status).build();
    }

    @PostMapping(value = "/signUp/{providerId}", params = "code")
    public ResponseEntity<?> singUpSocial(
            HttpServletRequest request,
            @PathVariable("providerId") String providerId,
            @RequestParam("code") String code,
            @RequestParam("redirectUri") String redirectUri
    ) {
        ProviderType providerType = ProviderType.fromId(providerId);

        if (providerType == null) {
            return ResponseEntity.badRequest().build();
        }

        AuthContext authContext = new AuthContext()
                .setProviderType(providerType)
                .setRequest(request)
                .setCode(code);

        return ResponseEntity.ok(authService.signUp(authContext, redirectUri));
    }

    @GetMapping("/signUp/confirmation")
    public ResponseEntity<?> confirmation(HttpServletRequest request) {
        HttpStatus status = authService.confirmation(request);

        return ResponseEntity.status(status).build();
    }

    @PostMapping(value = "/signUp/{providerId}", params = "error")
    public ResponseEntity<?> errorSignUpSocial(
            @RequestParam("error") String error,
            @RequestParam(value = "error_description", required = false) String errorDescription
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/signIn")
    public ResponseEntity<?> singIn(
            HttpServletResponse response,
            @Valid @RequestBody SignInRequest signInRequest,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(authService.auth(response, signInRequest));
    }

    @PostMapping("/signOut")
    public ResponseEntity<?> signOut(HttpServletRequest request, HttpServletResponse response) {
        User user = authService.signOut(request, response);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else {
            return ResponseEntity.ok().build();
        }
    }

    @GetMapping("/account")
    public ResponseEntity<?> getAccount() {
        RequestResult requestResult = authService.account();

        return ResponseEntity.status(requestResult.getStatus()).body(requestResult.getBody());
    }
}
