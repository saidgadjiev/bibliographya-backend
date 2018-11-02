package ru.saidgadjiev.bibliography.properties;

import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by said on 24.10.2018.
 */
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    public String secretKey() {
        return "Slumdog millionaire";
    }

    public SignatureAlgorithm alg() {
        return SignatureAlgorithm.HS512;
    }
}
