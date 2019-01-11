package ru.saidgadjiev.bibliography.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by said on 06.01.2019.
 */
//@ConfigurationProperties
public class FirebaseProperties {

    @Value("${app.firebase.database.url}")
    private String databaseUrl;

    @Value("${app.firebase.config.path}")
    private String configPath;

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public String getConfigPath() {
        return configPath;
    }

    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }
}
