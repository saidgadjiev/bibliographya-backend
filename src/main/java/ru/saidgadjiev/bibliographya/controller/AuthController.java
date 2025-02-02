package ru.saidgadjiev.bibliographya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliographya.auth.common.AuthContext;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.auth.social.ResponseType;
import ru.saidgadjiev.bibliographya.domain.AuthKey;
import ru.saidgadjiev.bibliographya.domain.SendVerificationResult;
import ru.saidgadjiev.bibliographya.domain.SignInResult;
import ru.saidgadjiev.bibliographya.domain.SignUpConfirmation;
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
    public ResponseEntity<String> oauth(@PathVariable("providerId") String providerId,
                                        @RequestParam("redirectUri") String redirectUri,
                                        @RequestParam("responseType") String responseTypeValue) {
        ProviderType providerType = ProviderType.fromId(providerId);

        if (providerType == null) {
            return ResponseEntity.badRequest().build();
        }

        ResponseType responseType = ResponseType.fromDesc(responseTypeValue);

        if (responseType == null) {
            return ResponseEntity.badRequest().build();
        }

        if (providerType.equals(ProviderType.SIMPLE)) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(authService.getOauthUrl(providerType, redirectUri, responseType));
    }

    @PostMapping("/signUp/confirm-finish")
    public ResponseEntity<?> confirmSignUpFinish(HttpServletRequest request,
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

        SignInResult signUpResult = authService.confirmSignUpFinish(authContext);

        return ResponseEntity.status(signUpResult.getStatus()).body(signUpResult.getUser());
    }

    @PostMapping("/signUp/confirm-start")
    public ResponseEntity<?> confirmSignUp(HttpServletRequest request,
                                           Locale locale,
                                           AuthKey authKey) throws MessagingException {
        SendVerificationResult sendVerificationResult = authService.confirmSignUpStart(request, locale, authKey);

        return ResponseEntity.status(sendVerificationResult.getStatus()).body(sendVerificationResult);
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
                .setProviderType(ProviderType.SIMPLE)
                .setRequest(request)
                .setBody(signUpRequest);

        HttpStatus status = authService.signUp(authContext);

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
                .setRedirectUri(redirectUri)
                .setCode(code);

        return ResponseEntity.ok(authService.signUp(authContext));
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

    @GetMapping("/account")
    public ResponseEntity<?> getAccount(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(authentication.getPrincipal());
    }
}
