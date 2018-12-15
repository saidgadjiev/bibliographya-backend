package ru.saidgadjiev.bibliography.domain;

/**
 * Created by said on 15.12.2018.
 */
public class BiographyFix {

    private Integer id;

    private String fixText;

    private Integer biographyId;

    private String fixerName;

    private FixStatus fixStatus;

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

    public FixStatus getFixStatus() {
        return fixStatus;
    }

    public void setFixStatus(FixStatus fixStatus) {
        this.fixStatus = fixStatus;
    }

    public enum FixStatus {
        PENDING(0),
        CLOSED(1);

        private int code;

        FixStatus(int code) {
            this.code = code;
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
