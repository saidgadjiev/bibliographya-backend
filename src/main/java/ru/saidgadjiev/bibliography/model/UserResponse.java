package ru.saidgadjiev.bibliography.model;

import java.util.Collection;

public class UserResponse {

    private String id;

    private String providerId;

    private BiographyResponse biography;

    private Collection<String> roles;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public BiographyResponse getBiography() {
        return biography;
    }

    public void setBiography(BiographyResponse biography) {
        this.biography = biography;
    }

    public Collection<String> getRoles() {
        return roles;
    }

    public void setRoles(Collection<String> roles) {
        this.roles = roles;
    }
}
