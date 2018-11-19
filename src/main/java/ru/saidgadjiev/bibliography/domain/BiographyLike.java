package ru.saidgadjiev.bibliography.domain;

/**
 * Created by said on 15.11.2018.
 */
public class BiographyLike {

    private String userName;

    private int biographyId;

    public BiographyLike(String userName, int biographyId) {
        this.userName = userName;
        this.biographyId = biographyId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getBiographyId() {
        return biographyId;
    }

    public void setBiographyId(int biographyId) {
        this.biographyId = biographyId;
    }
}
