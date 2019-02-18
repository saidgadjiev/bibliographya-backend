package ru.saidgadjiev.bibliographya.domain;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;

public class AccountResult {

    private HttpStatus status;

    private UserDetails account;

    public HttpStatus getStatus() {
        return status;
    }

    public AccountResult setStatus(HttpStatus status) {
        this.status = status;

        return this;
    }

    public UserDetails getAccount() {
        return account;
    }

    public AccountResult setAccount(UserDetails account) {
        this.account = account;

        return this;
    }
}
