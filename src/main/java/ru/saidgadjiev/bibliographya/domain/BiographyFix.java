package ru.saidgadjiev.bibliographya.domain;

/**
 * Created by said on 15.12.2018.
 */
public class BiographyFix {

    private Integer id;

    private String fixText;

    private int biographyId;

    private Biography biography;

    private Integer fixerId;

    private Biography fixer;

    private Integer creatorId;

    private Biography creator;

    private FixStatus status;

    private String info;

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

    public Biography getFixer() {
        return fixer;
    }

    public void setFixer(Biography fixer) {
        this.fixer = fixer;
    }

    public Integer getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Integer creatorId) {
        this.creatorId = creatorId;
    }

    public Biography getCreator() {
        return creator;
    }

    public void setCreator(Biography creator) {
        this.creator = creator;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public enum FixStatus {

        PENDING(0),

        CLOSED(1),

        IGNORED(2);

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
