package ru.saidgadjiev.bibliographya.domain;

import org.springframework.http.HttpStatus;

/**
 * Created by said on 23/04/2019.
 */
public class SendVerificationResult {

    private HttpStatus status;

    private Timer timer;

    public SendVerificationResult(HttpStatus status, Timer timer) {
        this.status = status;
        this.timer = timer;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }
}
