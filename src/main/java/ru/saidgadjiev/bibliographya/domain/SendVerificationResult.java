package ru.saidgadjiev.bibliographya.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.http.HttpStatus;

/**
 * Created by said on 23/04/2019.
 */
public class SendVerificationResult {

    @JsonIgnore
    private HttpStatus status;

    private String authKey;

    private Timer timer;

    public SendVerificationResult(HttpStatus status, Timer timer, String authKey) {
        this.status = status;
        this.timer = timer;
        this.authKey = authKey;
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

    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }
}
