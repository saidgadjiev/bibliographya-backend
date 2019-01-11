package ru.saidgadjiev.bibliography.model;

import ru.saidgadjiev.bibliography.bussiness.moderation.ModerationAction;

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

    private Integer creatorId;

    private Integer userId;

    private int likesCount;

    private long commentsCount;

    private boolean liked;

    private Timestamp updatedAt;

    private LastModified lastModified;

    private Integer moderationStatus;

    private Timestamp moderatedAt;

    private String moderatorId;

    private BiographyResponse moderatorBiography;

    private Collection<String> categories;

    private Collection<ModerationAction> actions;

    private String moderationInfo;

    private int publishStatus;

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

    public Integer getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Integer creatorId) {
        this.creatorId = creatorId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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
        if (updatedAt != null) {
            this.updatedAt = updatedAt;
            this.lastModified = new LastModified(updatedAt.getTime(), updatedAt.getNanos());
        }
    }

    public LastModified getLastModified() {
        return lastModified;
    }

    public Integer getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(Integer moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    public Timestamp getModeratedAt() {
        return moderatedAt;
    }

    public void setModeratedAt(Timestamp moderatedAt) {
        this.moderatedAt = moderatedAt;
    }

    public String getModeratorId() {
        return moderatorId;
    }

    public void setModeratorId(String moderatorId) {
        this.moderatorId = moderatorId;
    }

    public Collection<String> getCategories() {
        return categories;
    }

    public void setCategories(Collection<String> categories) {
        this.categories = categories;
    }

    public BiographyResponse getModeratorBiography() {
        return moderatorBiography;
    }

    public void setModeratorBiography(BiographyResponse moderatorBiography) {
        this.moderatorBiography = moderatorBiography;
    }

    public Collection<ModerationAction> getActions() {
        return actions;
    }

    public void setActions(Collection<ModerationAction> actions) {
        this.actions = actions;
    }

    public String getModerationInfo() {
        return moderationInfo;
    }

    public void setModerationInfo(String moderationInfo) {
        this.moderationInfo = moderationInfo;
    }

    public int getPublishStatus() {
        return publishStatus;
    }

    public void setPublishStatus(int publishStatus) {
        this.publishStatus = publishStatus;
    }
}
