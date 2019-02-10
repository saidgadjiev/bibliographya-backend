package ru.saidgadjiev.bibliographya.model;

import ru.saidgadjiev.bibliographya.domain.BiographyCategory;

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

    private String middleName;

    private String biography;

    private Integer userId;

    private LastModified lastModified;

    private Collection<Integer> addCategories;

    private Collection<Integer> deleteCategories;

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

    public Collection<Integer> getAddCategories() {
        return addCategories;
    }

    public void setAddCategories(Collection<Integer> addCategories) {
        this.addCategories = addCategories;
    }

    public Collection<Integer> getDeleteCategories() {
        return deleteCategories;
    }

    public void setDeleteCategories(Collection<Integer> deleteCategories) {
        this.deleteCategories = deleteCategories;
    }
}
