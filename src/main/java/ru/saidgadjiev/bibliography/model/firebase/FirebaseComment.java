package ru.saidgadjiev.bibliography.model.firebase;

/**
 * Created by said on 06.01.2019.
 */
public class FirebaseComment {

    private int userId;

    private int biographyId;

    public FirebaseComment(int userId, int biographyId) {
        this.userId = userId;
        this.biographyId = biographyId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBiographyId() {
        return biographyId;
    }

    public void setBiographyId(int biographyId) {
        this.biographyId = biographyId;
    }
}
