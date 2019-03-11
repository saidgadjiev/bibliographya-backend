package ru.saidgadjiev.bibliographya.domain;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class SignUpConfirmation extends EmailConfirmation {

    @NotNull
    @Size(min = 6)
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
