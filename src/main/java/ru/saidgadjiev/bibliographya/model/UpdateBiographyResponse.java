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

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
