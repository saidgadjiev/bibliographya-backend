package ru.saidgadjiev.bibliographya.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by said on 02.03.2019.
 */
@ConfigurationProperties(prefix = "ui")
public class UIProperties {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
