package ru.saidgadjiev.bibliographya.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage.upload")
public class StorageProperties {

    private String root;

    @Value("category-root")
    private String categoryRoot;

    @Value("share-root")
    private String shareRoot;

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getCategoryRoot() {
        return categoryRoot;
    }

    public void setCategoryRoot(String categoryRoot) {
        this.categoryRoot = categoryRoot;
    }

    public String getShareRoot() {
        return shareRoot;
    }

    public void setShareRoot(String shareRoot) {
        this.shareRoot = shareRoot;
    }
}
