package ru.saidgadjiev.bibliography.domain;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;

/**
 * Created by said on 22.10.2018.
 */
public class Biography {

    private int id;

    private String firstName;

    private String lastName;

    private String middleName;

    private String biography;

    private Integer creatorId;

    private Integer userId;

    private Timestamp updatedAt;

    private ModerationStatus moderationStatus;

    private Timestamp moderatedAt;

    private Integer moderatorId;

    private Collection<String> categories;

    private int likesCount;

    private long commentsCount;

    private long newComplaintsCount;

    private Collection<BiographyReport> newComplaints;

    private long oldComplaintsCount;

    private Collection<BiographyReport> oldComplaints;

    private boolean liked;

    private Biography moderatorBiography;

    private String moderationInfo;

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public Integer getModeratorId() {
        return moderatorId;
    }

    public void setModeratorId(Integer moderatorId) {
        this.moderatorId = moderatorId;
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

    public String getModerationInfo() {
        return moderationInfo;
    }

    public void setModerationInfo(String moderationInfo) {
        this.moderationInfo = moderationInfo;
    }

    public Collection<BiographyReport> getNewComplaints() {
        return newComplaints;
    }

    public void setNewComplaints(Collection<BiographyReport> newComplaints) {
        this.newComplaints = newComplaints;
    }

    public long getNewComplaintsCount() {
        return newComplaintsCount;
    }

    public void setNewComplaintsCount(long newComplaintsCount) {
        this.newComplaintsCount = newComplaintsCount;
    }

    public long getOldComplaintsCount() {
        return oldComplaintsCount;
    }

    public void setOldComplaintsCount(long oldComplaintsCount) {
        this.oldComplaintsCount = oldComplaintsCount;
    }

    public Collection<BiographyReport> getOldComplaints() {
        return oldComplaints;
    }

    public void setOldComplaints(Collection<BiographyReport> oldComplaints) {
        this.oldComplaints = oldComplaints;
    }

    /**
     * Created by said on 26.11.2018.
     */
    public enum ModerationStatus {

        PENDING(0),

        APPROVED(1),

        REJECTED(2);

        private int code;

        ModerationStatus(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static ModerationStatus fromCode(int code) {
            for (ModerationStatus moderationStatus : ModerationStatus.values()) {
                if (moderationStatus.code == code) {
                    return moderationStatus;
                }
            }

            return null;
        }
    }
}
