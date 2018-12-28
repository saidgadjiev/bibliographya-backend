package ru.saidgadjiev.bibliography.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Collection;

/**
 * Created by said on 18.11.2018.
 */
public class BiographyRequest {

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

    private Integer userId;

    private LastModified lastModified;

    private Collection<String> addedCategories;

    private Collection<String> deleteCategories;

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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public LastModified getLastModified() {
        return lastModified;
    }

    public void setLastModified(LastModified lastModified) {
        this.lastModified = lastModified;
    }

    public Collection<String> getAddedCategories() {
        return addedCategories;
    }

    public void setAddedCategories(Collection<String> addedCategories) {
        this.addedCategories = addedCategories;
    }

    public Collection<String> getDeleteCategories() {
        return deleteCategories;
    }

    public void setDeleteCategories(Collection<String> deleteCategories) {
        this.deleteCategories = deleteCategories;
    }
}
