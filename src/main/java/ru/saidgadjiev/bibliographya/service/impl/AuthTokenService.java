package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.service.api.TokenService;
import ru.saidgadjiev.bibliographya.utils.TimeUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by said on 26.12.2018.
 */
@Service
public class AuthTokenService {

    private final TokenService jwtTokenService;

    public AuthTokenService(TokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    public Map<String, Object> validate(String token) {
        Map<String, Object> details = jwtTokenService.validate(token);

        if (details == null) {
            return null;
        }

        Long expiredAt = (Long) details.get("expiredAt");

        if (TimeUtils.isExpired(expiredAt)) {
            return null;
        }

        return details;
    }

    public Map<String, Object> getClaims(String token) {
        return jwtTokenService.validate(token);
    }

    public String getUserId(String token) {
        Map<String, Object> details = jwtTokenService.validate(token);

        return (String) details.get("userId");
    }

    public String createToken(User user) {
        Map<String, Object> payload = new HashMap<String, Object>() {{
            put("providerId", user.getProviderType().getId());
            put("userId", user.getId());
        }};

        return jwtTokenService.generate(payload);
    }
}
