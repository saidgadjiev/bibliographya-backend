package ru.saidgadjiev.bibliographya.model;

import ru.saidgadjiev.bibliographya.domain.HasVerificationKey;
import ru.saidgadjiev.bibliographya.domain.AuthenticationKey;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by said on 28.10.2018.
 */
public class SignInRequest implements HasVerificationKey {

    private AuthenticationKey authenticationKey;

    @NotNull
    @Size(min = 1)
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
