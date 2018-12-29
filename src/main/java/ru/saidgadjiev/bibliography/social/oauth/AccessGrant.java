package ru.saidgadjiev.bibliography.social.oauth;

/**
 * Created by said on 29.12.2018.
 */
public class AccessGrant {

    private final String accessToken;

    private final Long expireTime;

    private String userId;

    public AccessGrant(String accessToken, Long expiresIn, String userId) {
        this.accessToken = accessToken;
        this.expireTime = expiresIn != null ? System.currentTimeMillis() + expiresIn * 1000L : null;
        this.userId = userId;
    }

    /**
     * The access token value.
     * @return The access token value.
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * The time (in milliseconds since Jan 1, 1970 UTC) when this access grant will expire.
     * May be null if the token is non-expiring.
     * @return The time (in milliseconds since Jan 1, 1970 UTC) when this access grant will expire.
     */
    public Long getExpireTime() {
        return expireTime;
    }

    public String getUserId() {
        return userId;
    }
}
