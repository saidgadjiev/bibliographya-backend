package ru.saidgadjiev.bibliography.domain;

import org.jetbrains.annotations.NotNull;

import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Created by said on 22.10.2018.
 */
public class Biography {

    private Integer id;

    private String firstName;

    private String lastName;

    private String middleName;

    private String biography;

    private String creatorName;

    private String userName;

    private Timestamp updatedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class Builder {

        private Integer id;

        private String firstName;

        private String lastName;

        private String middleName;

        private String biography;

        private String creatorName;

        private String userName;

        private Timestamp updatedAt;

        public Builder() {}

        public Builder(String firstName, String lastName, String middleName) {
            Objects.requireNonNull(firstName);
            Objects.requireNonNull(lastName);
            Objects.requireNonNull(middleName);
            this.firstName = firstName;
            this.lastName = lastName;
            this.middleName = middleName;
        }

        public Builder setFirstName(String firstName) {
            this.firstName = firstName;

            return this;
        }

        public Builder setLastName(String lastName) {
            this.lastName = lastName;

            return this;
        }

        public Builder setMiddleName(String middleName) {
            this.middleName = middleName;

            return this;
        }

        public Builder setId(Integer id) {
            this.id = id;

            return this;
        }

        public Builder setBiography(String biography) {
            this.biography = biography;

            return this;
        }

        public Builder setCreatorName(String creatorName) {
            this.creatorName = creatorName;

            return this;
        }

        public Builder setUserName(String userName) {
            this.userName = userName;

            return this;
        }

        public Builder setUpdatedAt(Timestamp updatedAt) {
            this.updatedAt = updatedAt;

            return this;
        }

        public Biography build() {
            Biography biography = new Biography();

            biography.setId(id);
            biography.setBiography(this.biography);
            biography.setCreatorName(creatorName);
            biography.setUserName(userName);
            biography.setFirstName(firstName);
            biography.setLastName(lastName);
            biography.setMiddleName(middleName);
            biography.setBiography(this.biography);
            biography.setUpdatedAt(this.updatedAt);

            return biography;
        }
    }
}
