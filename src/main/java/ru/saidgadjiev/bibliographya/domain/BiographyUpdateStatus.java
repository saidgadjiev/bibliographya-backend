package ru.saidgadjiev.bibliographya.domain;

import java.sql.Timestamp;

/**
 * Created by said on 21.11.2018.
 */
public class BiographyUpdateStatus {
    
    private final int updated;
    
    private final Timestamp updatedAt;

    private String bio;

    public BiographyUpdateStatus(int updated, Timestamp updatedAt) {
        this.updated = updated;
        this.updatedAt = updatedAt;
    }

    public int getUpdated() {
        return updated;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getBio() {
        return bio;
    }
}
