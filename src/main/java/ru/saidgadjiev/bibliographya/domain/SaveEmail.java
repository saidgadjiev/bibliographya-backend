package ru.saidgadjiev.bibliographya.domain;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

public class SaveEmail {

    @NotNull
    @Email
    private String newEmail;

    @NotNull
    private Integer code;

    public String getNewEmail() {
        return newEmail;
    }

    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
