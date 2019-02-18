package ru.saidgadjiev.bibliographya.domain;

import org.springframework.http.HttpStatus;

/**
 * Created by said on 11.02.2019.
 */
public class SignUpResult {

    private HttpStatus status;

    public HttpStatus getStatus() {
        return status;
    }

    public SignUpResult setStatus(HttpStatus status) {
        this.status = status;

        return this;
    }
}
