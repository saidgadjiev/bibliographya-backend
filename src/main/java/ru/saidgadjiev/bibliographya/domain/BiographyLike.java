package ru.saidgadjiev.bibliographya.domain;

/**
 * Created by said on 15.11.2018.
 */
public class BiographyLike {

    public static final String USER_ID = "user_id";

    private Integer userId;

    private Biography user;

    private Integer biographyId;

    public BiographyLike() {
    }

    public BiographyLike(Integer userId, int biographyId) {
        this.userId = userId;
        this.biographyId = biographyId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getBiographyId() {
        return biographyId;
    }

    public void setBiographyId(Integer biographyId) {
        this.biographyId = biographyId;
    }

    public Biography getUser() {
        return user;
    }

    public void setUser(Biography user) {
        this.user = user;
    }
}
