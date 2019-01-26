package ru.saidgadjiev.bibliographya.model;


import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by said on 04.11.2018.
 */
public class UpdateBiographyRequest {

    @NotNull
    @Size(min = 1)
    private String firstName;

    @NotNull
    @Size(min = 1)
    private String lastName;

    @NotNull
    @Size(min = 1)
    private String middleName;

    private String biography;

    @NotNull
    private LastModified lastModified;

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

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public void setLastModified(LastModified lastModified) {
        this.lastModified = lastModified;
    }

    public LastModified getLastModified() {
        return lastModified;
    }
}
