package ru.saidgadjiev.bibliographya.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by said on 01.01.2019.
 */
public class BiographyComplaintResponse {

    private int reason;

    private Collection<String> complaintTexts = new ArrayList<>();

    private int count;

    public int getReason() {
        return reason;
    }

    public void setReason(int reason) {
        this.reason = reason;
    }

    public Collection<String> getComplaintTexts() {
        return complaintTexts;
    }

    public void setComplaintTexts(Collection<String> complaintTexts) {
        this.complaintTexts = complaintTexts;
    }

    public void addComplainText(String text) {
        complaintTexts.add(text);
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
