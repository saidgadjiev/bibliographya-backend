package ru.saidgadjiev.bibliographya.model;

import ru.saidgadjiev.bibliographya.domain.HasAuthKey;
import ru.saidgadjiev.bibliographya.domain.AuthenticationKey;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class RestorePassword implements HasAuthKey {

    private AuthenticationKey authenticationKey;

    @NotNull
    @Size(min = 6)
    private String password;

    @NotNull
    private Integer code;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public AuthenticationKey getAuthenticationKey() {
        return authenticationKey;
    }

    @Override
    public void setAuthenticationKey(AuthenticationKey authenticationKey) {
        this.authenticationKey = authenticationKey;
    }
}
