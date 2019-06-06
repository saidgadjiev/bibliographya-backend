package ru.saidgadjiev.bibliographya.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ru.saidgadjiev.bibliographya.domain.jackson.TrimDeserializer;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by said on 18.03.2018.
 */
public class SignUpRequest {

    @NotNull
    @Size(min = 1)
    @JsonDeserialize(using = TrimDeserializer.class)
    private String firstName;

    @NotNull
    @Size(min = 1)
    @JsonDeserialize(using = TrimDeserializer.class)
    private String lastName;

    @JsonDeserialize(using = TrimDeserializer.class)
    private String middleName;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }
}
