package ru.saidgadjiev.bibliography.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliography.auth.ProviderType;
import ru.saidgadjiev.bibliography.domain.User;
import ru.saidgadjiev.bibliography.model.SignInRequest;
import ru.saidgadjiev.bibliography.model.SignUpRequest;
import ru.saidgadjiev.bibliography.service.api.UserService;
import ru.saidgadjiev.bibliography.service.impl.auth.AuthContext;
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

    private final UserService userService;

    private final AuthService authService;

    @Autowired
    public AuthController(UserService userService,
                          AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/signUp")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest signUpRequest, BindingResult bindingResult) throws SQLException {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }
        authService.signUp(signUpRequest);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/signIn")
    public ResponseEntity<?> singIn(HttpServletResponse response, @Valid @RequestBody SignInRequest signInRequest, BindingResult bindingResult) throws SQLException {
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

    @RequestMapping(value = "/exist/{username}", method = RequestMethod.HEAD)
    public ResponseEntity existUserName(@PathVariable(value = "username") String username) throws SQLException {
        if (userService.isExistUserName(username)) {
            return ResponseEntity.status(HttpStatus.FOUND).build();
        }

        return ResponseEntity.ok().build();
    }
}
