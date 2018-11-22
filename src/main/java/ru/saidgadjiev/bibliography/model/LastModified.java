package ru.saidgadjiev.bibliography.model;

/**
 * Created by said on 21.11.2018.
 */
public class LastModified {

    private long time;

    private int nanos;

    public LastModified() {
    }

    public LastModified(long time, int nanos) {
        this.time = time;
        this.nanos = nanos;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getNanos() {
        return nanos;
    }

    public void setNanos(int nanos) {
        this.nanos = nanos;
    }
}
