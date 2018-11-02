package ru.saidgadjiev.bibliography.service.api;

import io.jsonwebtoken.Jwts;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliography.properties.JwtProperties;

import java.util.Map;

/**
 * Created by said on 24.10.2018.
 */
@Service
@Qualifier("customTokenService")
public class JwtTokenServiceImpl implements TokenService {

    private static final Logger LOGGER = Logger.getLogger(JwtTokenServiceImpl.class);

    private JwtProperties jwtProperties;

    @Autowired
    public JwtTokenServiceImpl(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public String generate(Map<String, Object> payload) {
        return Jwts.builder()
                .setClaims(payload)
                .signWith(jwtProperties.alg(), jwtProperties.secretKey())
                .compact();
    }

    @Override
    public Map<String, Object> validate(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(jwtProperties.secretKey())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            LOGGER.error("Token validate exception " + e.getMessage(), e);
        }

        return null;
    }
}
