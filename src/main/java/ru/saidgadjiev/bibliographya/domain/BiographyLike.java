package ru.saidgadjiev.bibliographya.domain;

/**
 * Created by said on 15.11.2018.
 */
public class BiographyLike {

    private Integer userId;

    private int biographyId;

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

    public int getBiographyId() {
        return biographyId;
    }

    public void setBiographyId(int biographyId) {
        this.biographyId = biographyId;
    }
}
