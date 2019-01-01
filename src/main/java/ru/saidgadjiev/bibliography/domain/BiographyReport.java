package ru.saidgadjiev.bibliography.domain;

/**
 * Created by said on 31.12.2018.
 */
public class BiographyReport {

    private int id;

    private int reporterId;

    private int biographyId;

    private ReportStatus status;

    private ReportReason reason;

    private String reasonText;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReporterId() {
        return reporterId;
    }

    public void setReporterId(int reporterId) {
        this.reporterId = reporterId;
    }

    public int getBiographyId() {
        return biographyId;
    }

    public void setBiographyId(int biographyId) {
        this.biographyId = biographyId;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public ReportReason getReason() {
        return reason;
    }

    public void setReason(ReportReason reason) {
        this.reason = reason;
    }

    public String getReasonText() {
        return reasonText;
    }

    public void setReasonText(String reasonText) {
        this.reasonText = reasonText;
    }

    public enum ReportReason {

        SPAM(0),
        CLONE(1),
        ANOTHER(2);

        private int code;

        ReportReason(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static ReportReason fromCode(int code) {
            for (ReportReason reason: values()) {
                if (reason.code == code) {
                    return reason;
                }
            }

            return null;
        }
    }

    public enum  ReportStatus {

        PENDING(0),
        CONSIDERED(1);

        private int code;

        ReportStatus(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static ReportStatus fromCode(int code) {
            for (ReportStatus status: values()) {
                if (status.code == code) {
                    return status;
                }
            }

            return null;
        }
    }
}
