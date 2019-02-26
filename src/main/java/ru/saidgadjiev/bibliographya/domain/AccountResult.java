package ru.saidgadjiev.bibliographya.domain;

import org.springframework.http.HttpStatus;

public class AccountResult<T> {

    private HttpStatus status;

    private T body;

    public HttpStatus getStatus() {
        return status;
    }

    public AccountResult<T> setStatus(HttpStatus status) {
        this.status = status;

        return this;
    }

    public T getBody() {
        return body;
    }

    public AccountResult<T> setBody(T body) {
        this.body = body;

        return this;
    }
}
