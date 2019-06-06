package ru.saidgadjiev.bibliographya.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ru.saidgadjiev.bibliographya.configuration.BibliographyaConfiguration;
import ru.saidgadjiev.bibliographya.domain.jackson.TrimDeserializer;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by said on 18.11.2018.
 */
public class BiographyRequest {

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

    @JsonDeserialize(using = TrimDeserializer.class)
    private String bio;

    private Integer userId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BibliographyaConfiguration.DATE_FORMAT)
    private Timestamp updatedAt;

    private List<Integer> addCategories;

    private List<Integer> deleteCategories;

    @NotNull
    private Integer countryId;

    private List<Integer> addProfessions;

    private List<Integer> deleteProfessions;

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

    public Integer getCountryId() {
        return countryId;
    }

    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }

    public List<Integer> getAddProfessions() {
        return addProfessions;
    }

    public void setAddProfessions(List<Integer> addProfessions) {
        this.addProfessions = addProfessions;
    }

    public List<Integer> getDeleteProfessions() {
        return deleteProfessions;
    }

    public void setDeleteProfessions(List<Integer> deleteProfessions) {
        this.deleteProfessions = deleteProfessions;
    }
}
