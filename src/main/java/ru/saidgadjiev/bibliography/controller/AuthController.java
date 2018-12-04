package ru.saidgadjiev.bibliography.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.CompositeLogoutHandler;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliography.domain.User;
import ru.saidgadjiev.bibliography.model.SignInRequest;
import ru.saidgadjiev.bibliography.model.SignUpRequest;
import ru.saidgadjiev.bibliography.properties.JwtProperties;
import ru.saidgadjiev.bibliography.security.service.SecurityService;
import ru.saidgadjiev.bibliography.service.api.TokenService;
import ru.saidgadjiev.bibliography.service.api.UserService;
import ru.saidgadjiev.bibliography.service.impl.TokenCookieService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * Created by said on 22.10.2018.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    private final SecurityService securityService;

    private final TokenService tokenService;

    private final TokenCookieService tokenCookieService;

    private final JwtProperties jwtProperties;

    private LogoutHandler logoutHandler = new CompositeLogoutHandler(
        new CookieClearingLogoutHandler("X-TOKEN"),
        new SecurityContextLogoutHandler()
    );

    @Autowired
    public AuthController(UserService userService,
                          SecurityService securityService,
                          TokenService tokenService,
                          TokenCookieService tokenCookieService,
                          JwtProperties jwtProperties) {
        this.userService = userService;
        this.securityService = securityService;
        this.tokenService = tokenService;
        this.tokenCookieService = tokenCookieService;
        this.jwtProperties = jwtProperties;
    }

    @PostMapping("/signUp")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest signUpRequest, BindingResult bindingResult) throws SQLException {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }
        userService.save(signUpRequest);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/signIn")
    public ResponseEntity<?> singIn(HttpServletResponse response, @Valid @RequestBody SignInRequest signInRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Authentication authentication = securityService.signIn(signInRequest.getUsername(), signInRequest.getPassword());
            User user = (User) authentication.getPrincipal();
            String token = tokenService.generate(new HashMap<String, Object>() {{
                put("username", user.getUsername());
                put("authorities", user.getAuthorities());
            }});

            tokenCookieService.addCookie(response, "X-TOKEN", token);

            return ResponseEntity.ok(authentication.getPrincipal());
        } catch (BadCredentialsException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/signOut")
    public ResponseEntity<?> signOut(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = securityService.findLoggedInUserAuthentication();

        logoutHandler.logout(request, response, authentication);

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } else {
            return ResponseEntity.ok().build();
        }
    }

    @GetMapping("/account")
    public ResponseEntity<UserDetails> signOut() {
        UserDetails userDetails = securityService.findLoggedInUser();

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
