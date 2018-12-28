package ru.saidgadjiev.bibliography.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliography.auth.ProviderType;
import ru.saidgadjiev.bibliography.domain.User;
import ru.saidgadjiev.bibliography.service.impl.auth.AuthContext;
import ru.saidgadjiev.bibliography.service.impl.auth.AuthService;

import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;

/**
 * Created by said on 23.12.2018.
 */
@RestController
@RequestMapping("/api/auth/social")
public class SocialAuthController {

    private final AuthService authService;

    public SocialAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/oauth/{providerId}")
    public ResponseEntity<String> signIn(@PathVariable("providerId") String providerId) {
        ProviderType providerType = ProviderType.fromId(providerId);

        if (providerType == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(authService.getOauthUrl(providerType));
    }

    @PostMapping("/signIn/{providerId}")
    public ResponseEntity<?> signInCallback(HttpServletResponse response, @PathVariable("providerId") String providerId, @RequestParam("code") String code) throws SQLException {
        ProviderType providerType = ProviderType.fromId(providerId);

        if (providerType == null) {
            return ResponseEntity.badRequest().build();
        }

        AuthContext authContext = new AuthContext()
                .setResponse(response)
                .setCode(code)
                .setProviderType(providerType);

        User user = authService.auth(authContext);

        return ResponseEntity.ok(user);
    }
}
