package ru.saidgadjiev.bibliographya.properties;

import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by said on 24.10.2018.
 */
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    
    public static final String VERIFICATION_TOKEN = "tjwt";

    private String secret;

    public String secret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public SignatureAlgorithm alg() {
        return SignatureAlgorithm.HS512;
    }

    public String tokenName() {
        return "X-TOKEN";
    }
}
