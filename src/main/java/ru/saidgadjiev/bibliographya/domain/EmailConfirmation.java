package ru.saidgadjiev.bibliographya.domain;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

public class EmailConfirmation {

    @NotNull
    @Email
    private String email;

    @NotNull
    private Integer code;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
