package ru.saidgadjiev.bibliographya.domain;

/**
 * Created by said on 31.12.2018.
 */
public class BiographyReportRequest {

    private int reason;

    private String reasonText;

    public int getReason() {
        return reason;
    }

    public void setReason(int reason) {
        this.reason = reason;
    }

    public String getReasonText() {
        return reasonText;
    }

    public void setReasonText(String reasonText) {
        this.reasonText = reasonText;
    }
}
