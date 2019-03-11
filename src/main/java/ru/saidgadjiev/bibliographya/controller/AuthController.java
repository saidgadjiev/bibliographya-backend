package ru.saidgadjiev.bibliographya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliographya.auth.common.AuthContext;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.domain.RequestResult;
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

    @PostMapping("/signUp")
    public ResponseEntity<?> signUp(HttpServletRequest request,
                                    Locale locale,
                                    @Valid @RequestBody SignUpRequest signUpRequest,
                                    BindingResult bindingResult) throws MessagingException {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }
        HttpStatus status = authService.signUp(request, locale, signUpRequest);

        return ResponseEntity.status(status).build();
    }

    @PostMapping("/signUp/confirm")
    public ResponseEntity<?> confirmSignUp(HttpServletRequest request,
                                           HttpServletResponse response,
                                           @RequestParam("code") Integer code) throws SQLException {
        if (code == null) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED).build();
        }

        AuthContext authContext = new AuthContext()
                .setRequest(request)
                .setResponse(response);

        SignUpResult signUpResult = authService.confirmSignUp(authContext, code);

        return ResponseEntity.status(signUpResult.getStatus()).body(signUpResult.getUser());
    }

    @PostMapping("/signUp/cancel")
    public ResponseEntity<?> cancelSignUp(HttpServletRequest request) throws SQLException {
        authService.cancelSignUp(request);

        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/signUp/{providerId}", params = "code")
    public ResponseEntity<?> singInSocial(
            HttpServletRequest request,
            HttpServletResponse response,
            @PathVariable("providerId") String providerId,
            @RequestParam("code") String code,
            @RequestParam("redirectUri") String redirectUri
    ) throws SQLException {
        ProviderType providerType = ProviderType.fromId(providerId);

        if (providerType == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            AuthContext authContext = new AuthContext()
                    .setProviderType(providerType)
                    .setRequest(request)
                    .setCode(code)
                    .setResponse(response);

            return ResponseEntity.ok(authService.auth(authContext, redirectUri));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(value = "/signIn/{providerId}", params = "error")
    public ResponseEntity<?> errorSignInSocial(
            @RequestParam("error") String error,
            @RequestParam(value = "error_description", required = false) String errorDescription
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/signIn")
    public ResponseEntity<?> singIn(
            HttpServletRequest request,
            HttpServletResponse response,
            @Valid @RequestBody SignInRequest signInRequest,
            BindingResult bindingResult
    ) throws SQLException {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        AuthContext authContext = new AuthContext()
                .setProviderType(ProviderType.EMAIL_PASSWORD)
                .setResponse(response)
                .setRequest(request)
                .setSignUpRequest(signInRequest);

        return ResponseEntity.ok(authService.auth(authContext, null));
    }

    @GetMapping("/signUp/confirmation")
    public ResponseEntity<?> confirmation(HttpServletRequest request) {
        RequestResult<?> requestResult = authService.confirmation(request);

        return ResponseEntity.status(requestResult.getStatus()).body(requestResult.getBody());
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
