package ru.saidgadjiev.bibliographya.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by said on 02.03.2019.
 */
@ConfigurationProperties(prefix = "ui")
public class UIProperties {

    private String host;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
