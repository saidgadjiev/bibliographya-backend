package ru.saidgadjiev.bibliography.domain;

/**
 * Created by said on 15.12.2018.
 */
public class BiographyFix {

    private Integer id;

    private String fixText;

    private Integer biographyId;

    private Biography biography;

    private String fixerName;

    private Biography fixerBiography;

    private String creatorName;

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

    public Integer getBiographyId() {
        return biographyId;
    }

    public void setBiographyId(Integer biographyId) {
        this.biographyId = biographyId;
    }

    public String getFixerName() {
        return fixerName;
    }

    public void setFixerName(String fixerName) {
        this.fixerName = fixerName;
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

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
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
