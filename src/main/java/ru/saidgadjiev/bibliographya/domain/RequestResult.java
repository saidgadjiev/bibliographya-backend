package ru.saidgadjiev.bibliographya.domain;

import org.springframework.http.HttpStatus;

public class RequestResult<T> {

    private HttpStatus status;

    private T body;

    public HttpStatus getStatus() {
        return status;
    }

    public RequestResult<T> setStatus(HttpStatus status) {
        this.status = status;

        return this;
    }

    public T getBody() {
        return body;
    }

    public RequestResult<T> setBody(T body) {
        this.body = body;

        return this;
    }
}
