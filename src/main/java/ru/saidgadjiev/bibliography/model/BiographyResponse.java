package ru.saidgadjiev.bibliography.model;

import java.sql.Timestamp;
import java.util.Collection;

/**
 * Created by said on 22.10.2018.
 */
public class BiographyResponse {

    private Integer id;

    private String firstName;

    private String lastName;

    private String middleName;

    private String biography;

    private String creatorName;

    private String userName;

    private int likesCount;

    private long commentsCount;

    private boolean liked;

    private Timestamp updatedAt;

    private LastModified lastModified;

    private int moderationStatus;

    private Timestamp moderatorAt;

    private String moderatorName;

    private Collection<String> categories;

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

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public long getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(long commentsCount) {
        this.commentsCount = commentsCount;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
        this.lastModified = new LastModified(updatedAt.getTime(), updatedAt.getNanos());
    }

    public LastModified getLastModified() {
        return lastModified;
    }

    public int getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(int moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    public Timestamp getModeratorAt() {
        return moderatorAt;
    }

    public void setModeratorAt(Timestamp moderatorAt) {
        this.moderatorAt = moderatorAt;
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
}
