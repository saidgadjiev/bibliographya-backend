package ru.saidgadjiev.bibliographya.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ru.saidgadjiev.bibliographya.domain.jackson.TrimDeserializer;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class SignUpConfirmation extends AuthenticationKeyConfirmation {

    @NotNull
    @Size(min = 6)
    @JsonDeserialize(using = TrimDeserializer.class)
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
