package ru.saidgadjiev.bibliographya.properties;

/**
 * Created by said on 04.01.2019.
 */
//@ConfigurationProperties(prefix = "app.pusher")
public class PusherProperties {

    private String appId;

    private String key;

    private String secret;

    private String cluster;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }
}
