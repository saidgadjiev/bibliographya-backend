package ru.saidgadjiev.bibliographya.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage.upload")
public class StorageProperties {

    public static final String CATEGORY_ROOT = "category";

    public static final String BIOGRAPHY_ROOT = "biography";

    public static final String TEMP_ROOT = "temp";

    private String root;

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }
}
