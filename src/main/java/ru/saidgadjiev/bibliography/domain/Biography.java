package ru.saidgadjiev.bibliography.domain;

import ru.saidgadjiev.bibliography.model.ModerationStatus;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Objects;

/**
 * Created by said on 22.10.2018.
 */
public class Biography {

    public static final String MODERATION_STATUS = "moderation_status";

    public static final String MODERATOR_NAME = "moderator_name";

    private Integer id;

    private String firstName;

    private String lastName;

    private String middleName;

    private String biography;

    private String creatorName;

    private String userName;

    private Timestamp updatedAt;

    private ModerationStatus moderationStatus;

    private Timestamp moderatedAt;

    private String moderatorName;

    private Collection<String> categories;

    private int likesCount;

    private long commentsCount;

    private boolean liked;

    private Biography moderatorBiography;

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

    public ModerationStatus getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(ModerationStatus moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    public Timestamp getModeratedAt() {
        return moderatedAt;
    }

    public void setModeratedAt(Timestamp moderatedAt) {
        this.moderatedAt = moderatedAt;
    }

    public String getModeratorName() {
        return moderatorName;
    }

    public void setModeratorName(String moderatorName) {
        this.moderatorName = moderatorName;
    }

    public Collection<String> getCategories() {
        return categories;
    }

    public void setCategories(Collection<String> categories) {
        this.categories = categories;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public long getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(long commentsCount) {
        this.commentsCount = commentsCount;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public Biography getModeratorBiography() {
        return moderatorBiography;
    }

    public void setModeratorBiography(Biography moderatorBiography) {
        this.moderatorBiography = moderatorBiography;
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

        private ModerationStatus moderationStatus;

        private String moderatorName;

        private Timestamp moderatedAt;

        private Biography moderatorBiography;

        public Builder() {}

        public Builder(String firstName, String lastName, String middleName) {
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

        public Builder setModerationStatus(ModerationStatus moderationStatus) {
            this.moderationStatus = moderationStatus;

            return this;
        }

        public Builder setModeratorName(String moderatorName) {
            this.moderatorName = moderatorName;

            return this;
        }

        public Builder setModeratedAt(Timestamp moderatedAt) {
            this.moderatedAt = moderatedAt;

            return this;
        }

        public Biography getModeratorBiography() {
            return moderatorBiography;
        }

        public void setModeratorBiography(Biography moderatorBiography) {
            this.moderatorBiography = moderatorBiography;
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
            biography.setModerationStatus(moderationStatus);
            biography.setModeratedAt(moderatedAt);
            biography.setModeratorName(moderatorName);
            biography.setModeratorBiography(moderatorBiography);

            return biography;
        }
    }
}
