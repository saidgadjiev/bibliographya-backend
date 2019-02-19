package ru.saidgadjiev.bibliographya.domain;

import org.springframework.http.HttpStatus;

public class AccountResult {

    private HttpStatus status;

    private User account;

    public HttpStatus getStatus() {
        return status;
    }

    public AccountResult setStatus(HttpStatus status) {
        this.status = status;

        return this;
    }

    public User getAccount() {
        return account;
    }

    public AccountResult setAccount(User account) {
        this.account = account;

        return this;
    }
}
