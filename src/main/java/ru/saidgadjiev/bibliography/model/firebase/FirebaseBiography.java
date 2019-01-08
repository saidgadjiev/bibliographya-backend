package ru.saidgadjiev.bibliography.model.firebase;

/**
 * Created by said on 06.01.2019.
 */
public class FirebaseBiography {

    private int id;

    public FirebaseBiography(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
