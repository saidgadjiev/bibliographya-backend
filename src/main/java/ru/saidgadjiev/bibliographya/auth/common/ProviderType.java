package ru.saidgadjiev.bibliographya.auth.common;

/**
 * Created by said on 24.12.2018.
 */
public enum ProviderType {

    FACEBOOK("facebook"),
    VK("vk"),
    USERNAME_PASSWORD("username_password");

    private final String id;

    ProviderType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static ProviderType fromId(String id) {
        for (ProviderType type: values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }

        return null;
    }
}
