package ru.saidgadjiev.bibliographya.service.impl;

import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.properties.JwtProperties;
import ru.saidgadjiev.bibliographya.service.api.TokenService;

import java.util.Map;

/**
 * Created by said on 24.10.2018.
 */
@Service
@Qualifier("customTokenService")
public class JwtTokenServiceImpl implements TokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenServiceImpl.class);

    private JwtProperties jwtProperties;

    @Autowired
    public JwtTokenServiceImpl(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public String generate(Map<String, Object> payload) {
        return Jwts.builder()
                .setClaims(payload)
                .signWith(jwtProperties.alg(), jwtProperties.secret())
                .compact();
    }

    @Override
    public Map<String, Object> validate(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(jwtProperties.secret())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            LOGGER.error("Token validate exception " + e.getMessage(), e);
        }

        return null;
    }
}
