package ru.saidgadjiev.bibliography.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by said on 23.12.2018.
 */
@ConfigurationProperties(prefix = "spring.social.facebook")
public class FacebookProperties {

    private String appId;

    private String appSecret;

    private String appToken;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }
}
