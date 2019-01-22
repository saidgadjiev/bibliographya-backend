package ru.saidgadjiev.bibliography.domain;

import org.springframework.http.HttpStatus;

import java.sql.Timestamp;

/**
 * Created by said on 21.11.2018.
 */
public class BiographyUpdateStatus {
    
    private final int updated;
    
    private final Timestamp updatedAt;

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
}
