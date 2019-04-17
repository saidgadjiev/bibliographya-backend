package ru.saidgadjiev.bibliographya.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.saidgadjiev.bibliographya.configuration.BibliographyaConfiguration;

import java.sql.Timestamp;

/**
 * Created by said on 21.11.2018.
 */
public class UpdateBiographyResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BibliographyaConfiguration.DATE_FORMAT)
    private Timestamp updatedAt;

    private String bio;

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
