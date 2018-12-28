package ru.saidgadjiev.bibliography.service.impl;

import org.springframework.social.oauth2.AccessGrant;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliography.auth.ProviderType;
import ru.saidgadjiev.bibliography.domain.User;
import ru.saidgadjiev.bibliography.service.impl.auth.social.FacebookService;
import ru.saidgadjiev.bibliography.service.impl.auth.social.TokenInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by said on 26.12.2018.
 */
@Service
public class TokenService {

    private final JwtTokenServiceImpl jwtTokenService;

    private final FacebookService facebookService;

    public TokenService(JwtTokenServiceImpl jwtTokenService, FacebookService facebookService) {
        this.jwtTokenService = jwtTokenService;
        this.facebookService = facebookService;
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

        ProviderType providerType = ProviderType.fromId((String) details.get("providerId"));

        switch (providerType) {
            case FACEBOOK:
                String accessToken = (String) details.get("accessToken");
                AccessGrant accessGrant = new AccessGrant(accessToken, null, null, expiredAt);

                TokenInfo tokenInfo = facebookService.checkToken(accessGrant);

                if (!tokenInfo.isValid()) {
                    return null;
                }
                break;
            case USERNAME_PASSWORD:
                break;
        }

        return details;
    }

    public String createToken(User user, AccessGrant accessGrant) {
        Map<String, Object> payload = new HashMap<String, Object>() {{
            put("providerId", user.getProviderType().getId());
            put("userId", user.getId());
            put("authorities", user.getAuthorities());
        }};

        switch (user.getProviderType()) {
            case FACEBOOK:
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
