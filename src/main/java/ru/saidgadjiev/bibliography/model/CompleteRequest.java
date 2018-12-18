package ru.saidgadjiev.bibliography.model;

/**
 * Created by said on 18.12.2018.
 */
public class CompleteRequest {

    private String signal;

    private int status;

    private String rejectText;

    public String getSignal() {
        return signal;
    }

    public void setSignal(String signal) {
        this.signal = signal;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getRejectText() {
        return rejectText;
    }

    public void setRejectText(String rejectText) {
        this.rejectText = rejectText;
    }
}
