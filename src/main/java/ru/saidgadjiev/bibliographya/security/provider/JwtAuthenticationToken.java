package ru.saidgadjiev.bibliographya.security.provider;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Map;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final Map<String, Object> claims;

    public JwtAuthenticationToken(Map<String, Object> claims) {
        super(null);

        this.claims = claims;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return claims == null ? null : claims.get("userId");
    }
}
