package ru.saidgadjiev.bibliographya.model;

import java.util.Collection;

public class UserResponse {

    private Integer id;

    private BiographyResponse biography;

    private Collection<String> roles;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
