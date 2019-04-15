package ru.saidgadjiev.bibliographya.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.saidgadjiev.bibliographya.configuration.BibliographyaConfiguration;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

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

    private String bio;

    private Integer userId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BibliographyaConfiguration.DATE_FORMAT)
    private Timestamp updatedAt;

    private List<Integer> addCategories;

    private List<Integer> deleteCategories;

    private Collection<String> deleteImagePaths;

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

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Integer> getAddCategories() {
        return addCategories;
    }

    public void setAddCategories(List<Integer> addCategories) {
        this.addCategories = addCategories;
    }

    public List<Integer> getDeleteCategories() {
        return deleteCategories;
    }

    public void setDeleteCategories(List<Integer> deleteCategories) {
        this.deleteCategories = deleteCategories;
    }

    public Collection<String> getDeleteImagePaths() {
        return deleteImagePaths;
    }

    public void setDeleteImagePaths(Collection<String> deleteImagePaths) {
        this.deleteImagePaths = deleteImagePaths;
    }
}
