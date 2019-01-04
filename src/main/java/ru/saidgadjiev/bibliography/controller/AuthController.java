package ru.saidgadjiev.bibliography.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliography.auth.common.AuthContext;
import ru.saidgadjiev.bibliography.auth.common.ProviderType;
import ru.saidgadjiev.bibliography.domain.User;
import ru.saidgadjiev.bibliography.model.SignInRequest;
import ru.saidgadjiev.bibliography.model.SignUpRequest;
import ru.saidgadjiev.bibliography.service.impl.auth.AuthService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.sql.SQLException;

/**
 * Created by said on 22.10.2018.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/oauth/{providerId}")
    public ResponseEntity<String> signIn(@PathVariable("providerId") String providerId) {
        ProviderType providerType = ProviderType.fromId(providerId);

        if (providerType == null) {
            return ResponseEntity.badRequest().build();
        }

        if (providerType.equals(ProviderType.USERNAME_PASSWORD)) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(authService.getOauthUrl(providerType));
    }

    @PostMapping("/signUp")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest signUpRequest, BindingResult bindingResult) throws SQLException {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }
        authService.signUp(signUpRequest);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/signIn/{providerId}")
    public ResponseEntity<?> singInSocial(
            HttpServletResponse response,
            @PathVariable("providerId") String providerId,
            @RequestParam("code") String code
    ) throws SQLException {
        ProviderType providerType = ProviderType.fromId(providerId);

        if (providerType == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            AuthContext authContext = new AuthContext()
                    .setProviderType(providerType)
                    .setCode(code)
                    .setResponse(response);

            return ResponseEntity.ok(authService.auth(authContext));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/signIn")
    public ResponseEntity<?> singIn(
            HttpServletResponse response,
            @Valid @RequestBody SignInRequest signInRequest,
            BindingResult bindingResult
    ) throws SQLException {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            AuthContext authContext = new AuthContext()
                    .setProviderType(ProviderType.USERNAME_PASSWORD)
                    .setResponse(response)
                    .setSignInRequest(signInRequest);

            return ResponseEntity.ok(authService.auth(authContext));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.badRequest().build();
        }
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
    public ResponseEntity<UserDetails> signOut() {
        UserDetails userDetails = authService.account();

        if (userDetails == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(userDetails);
    }
}
