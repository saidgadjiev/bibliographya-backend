package ru.saidgadjiev.bibliography.service.impl;

import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliography.domain.User;
import ru.saidgadjiev.bibliography.auth.social.AccessGrant;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by said on 26.12.2018.
 */
@Service
public class TokenService {

    private final JwtTokenServiceImpl jwtTokenService;

    public TokenService(JwtTokenServiceImpl jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    public Map<String, Object> validate(String token) {
        Map<String, Object> details = jwtTokenService.validate(token);

        if (details == null) {
            return null;
        }

        Long expiredAt = (Long) details.get("expiredAt");

        if (expiredAt != null && isExpired(expiredAt)) {
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

    public String createToken(User user, AccessGrant accessGrant) {
        Map<String, Object> payload = new HashMap<String, Object>() {{
            put("providerId", user.getProviderType().getId());
            put("userId", user.getId());
            put("authorities", user.getAuthorities());
        }};

        switch (user.getProviderType()) {
            case FACEBOOK:
                payload.put("accountId", user.getSocialAccount().getAccountId());
                payload.put("expiredAt", accessGrant.getExpireTime());
                payload.put("accessToken", accessGrant.getAccessToken());

                break;
            case USERNAME_PASSWORD:
                break;
        }

        return jwtTokenService.generate(payload);
    }

    private boolean isExpired(Long expireTime) {
        return expireTime != null && System.currentTimeMillis() >= expireTime;
    }
}
