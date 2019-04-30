package ru.saidgadjiev.bibliographya.domain;

import javax.validation.constraints.NotNull;

public class AuthenticationKeyConfirmation {

    @NotNull
    private Integer code;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
