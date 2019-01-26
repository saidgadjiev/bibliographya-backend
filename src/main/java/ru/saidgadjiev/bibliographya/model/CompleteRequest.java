package ru.saidgadjiev.bibliographya.model;

/**
 * Created by said on 18.12.2018.
 */
public class CompleteRequest {

    private String signal;

    private int status;

    private String info;

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

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
