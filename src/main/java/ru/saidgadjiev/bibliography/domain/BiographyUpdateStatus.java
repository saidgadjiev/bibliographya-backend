package ru.saidgadjiev.bibliography.domain;

import org.springframework.http.HttpStatus;

import java.sql.Timestamp;

/**
 * Created by said on 21.11.2018.
 */
public class BiographyUpdateStatus {
    
    private final boolean updated;
    
    private final Timestamp updatedAt;

    public BiographyUpdateStatus(boolean updated, Timestamp updatedAt) {
        this.updated = updated;
        this.updatedAt = updatedAt;
    }

    public boolean isUpdated() {
        return updated;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
}
