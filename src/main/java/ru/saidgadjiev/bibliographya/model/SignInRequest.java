package ru.saidgadjiev.bibliographya.model;

import ru.saidgadjiev.bibliographya.domain.HasAuthKey;
import ru.saidgadjiev.bibliographya.domain.AuthKey;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by said on 28.10.2018.
 */
public class SignInRequest implements HasAuthKey {

    private AuthKey authKey;

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
    public AuthKey getAuthKey() {
        return authKey;
    }

    @Override
    public void setAuthKey(AuthKey authKey) {
        this.authKey = authKey;
    }
}
