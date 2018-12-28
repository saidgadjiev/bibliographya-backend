package ru.saidgadjiev.bibliography.domain;

/**
 * Created by said on 15.12.2018.
 */
public class BiographyFix {

    private Integer id;

    private String fixText;

    private int biographyId;

    private Biography biography;

    private Integer fixerId;

    private Biography fixerBiography;

    private Integer creatorId;

    private Biography creatorBiography;

    private FixStatus status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFixText() {
        return fixText;
    }

    public void setFixText(String fixText) {
        this.fixText = fixText;
    }

    public int getBiographyId() {
        return biographyId;
    }

    public void setBiographyId(int biographyId) {
        this.biographyId = biographyId;
    }

    public Integer getFixerId() {
        return fixerId;
    }

    public void setFixerId(Integer fixerId) {
        this.fixerId = fixerId;
    }

    public FixStatus getStatus() {
        return status;
    }

    public void setStatus(FixStatus status) {
        this.status = status;
    }

    public Biography getBiography() {
        return biography;
    }

    public void setBiography(Biography biography) {
        this.biography = biography;
    }

    public Biography getFixerBiography() {
        return fixerBiography;
    }

    public void setFixerBiography(Biography fixerBiography) {
        this.fixerBiography = fixerBiography;
    }

    public Integer getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Integer creatorId) {
        this.creatorId = creatorId;
    }

    public Biography getCreatorBiography() {
        return creatorBiography;
    }

    public void setCreatorBiography(Biography creatorBiography) {
        this.creatorBiography = creatorBiography;
    }

    public enum FixStatus {

        PENDING(0),

        CLOSED(1);

        private int code;

        FixStatus(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static FixStatus fromCode(int code) {
            for (FixStatus status: values()) {
                if (status.code == code) {
                    return status;
                }
            }

            return null;
        }
    }
}
