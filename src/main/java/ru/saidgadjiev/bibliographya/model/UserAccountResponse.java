package ru.saidgadjiev.bibliographya.model;

public class UserAccountResponse {

    private BiographyResponse biography;

    public BiographyResponse getBiography() {
        return biography;
    }

    public void setBiography(BiographyResponse biography) {
        this.biography = biography;
    }
}
