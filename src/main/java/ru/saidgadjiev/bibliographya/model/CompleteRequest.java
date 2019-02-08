package ru.saidgadjiev.bibliographya.model;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompleteRequest that = (CompleteRequest) o;
        return status == that.status &&
                Objects.equals(signal, that.signal) &&
                Objects.equals(info, that.info);
    }

    @Override
    public int hashCode() {
        return Objects.hash(signal, status, info);
    }
}
