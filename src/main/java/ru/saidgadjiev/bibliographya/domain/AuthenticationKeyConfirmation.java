package ru.saidgadjiev.bibliographya.domain;

import javax.validation.constraints.NotNull;

public class AuthenticationKeyConfirmation implements HasVerificationKey {

    private AuthenticationKey authenticationKey;

    @NotNull
    private Integer code;

    @Override
    public AuthenticationKey getAuthenticationKey() {
        return authenticationKey;
    }

    @Override
    public void setAuthenticationKey(AuthenticationKey authenticationKey) {
        this.authenticationKey = authenticationKey;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
