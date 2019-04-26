package ru.saidgadjiev.bibliographya.domain;

import org.springframework.http.HttpStatus;

/**
 * Created by said on 23/04/2019.
 */
public class SendVerificationResult {

    private HttpStatus status;

    private String tjwt;

    public SendVerificationResult(HttpStatus status, String tjwt) {
        this.status = status;
        this.tjwt = tjwt;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public String getTjwt() {
        return tjwt;
    }

    public void setTjwt(String tjwt) {
        this.tjwt = tjwt;
    }
}
