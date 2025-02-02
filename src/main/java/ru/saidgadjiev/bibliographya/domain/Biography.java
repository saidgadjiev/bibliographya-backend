package ru.saidgadjiev.bibliographya.domain;

import ru.saidgadjiev.bibliographya.html.Header;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Objects;

/**
 * Created by said on 22.10.2018.
 */
@SuppressWarnings("CPD-START")
public class Biography {

    public static final String TABLE = "biography";

    public static final String ID = "id";

    public static final String IS_LIKED = "is_liked";

    public static final String CREATOR_ID = "creator_id";

    public static final String USER_ID = "user_id";

    public static final String MODERATION_STATUS = "moderation_status";

    public static final String FIRST_NAME = "first_name";

    public static final String LAST_NAME = "last_name";

    public static final String MIDDLE_NAME = "middle_name";

    public static final String PUBLISH_STATUS = "publish_status";

    public static final String BIO = "bio";

    public static final String UPDATED_AT = "updated_at";

    public static final String MODERATED_AT = "moderated_at";

    public static final String MODERATOR_ID = "moderator_id";

    public static final String ANONYMOUS_CREATOR = "anonymous_creator";

    public static final String DISABLE_COMMENTS = "disable_comments";

    public static final String CREATED_AT = "created_at";

    public static final String COUNTRY_ID = "country_id";

    private Integer id;

    private String firstName;

    private String lastName;

    private String middleName;

    private String bio;

    private Integer creatorId;

    private Integer userId;

    private Timestamp updatedAt;

    private ModerationStatus moderationStatus;

    private Timestamp moderatedAt;

    private Timestamp createdAt;

    private Integer moderatorId;

    private Collection<BiographyCategory> categories;

    private long likesCount;

    private long commentsCount;

    private long viewsCount;

    private long newComplaintsCount;

    private Collection<BiographyReport> newComplaints;

    private long oldComplaintsCount;

    private Collection<BiographyReport> oldComplaints;

    private boolean liked;

    private Biography moderator;

    private Biography creator;

    private String moderationInfo;

    private PublishStatus publishStatus;

    private Collection<Header> headers;

    private boolean disableComments;

    private boolean anonymousCreator;

    private Integer countryId;

    private Country country;

    private Collection<Profession> professions;

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

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getModeratorId() {
        return moderatorId;
    }

    public void setModeratorId(Integer moderatorId) {
        this.moderatorId = moderatorId;
    }

    public Collection<BiographyCategory> getCategories() {
        return categories;
    }

    public void setCategories(Collection<BiographyCategory> categories) {
        this.categories = categories;
    }

    public long getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(long likesCount) {
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

    public Biography getModerator() {
        return moderator;
    }

    public void setModerator(Biography moderator) {
        this.moderator = moderator;
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

    public PublishStatus getPublishStatus() {
        return publishStatus;
    }

    public void setPublishStatus(PublishStatus publishStatus) {
        this.publishStatus = publishStatus;
    }

    public Biography getCreator() {
        return creator;
    }

    public void setCreator(Biography creator) {
        this.creator = creator;
    }

    public Collection<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(Collection<Header> headers) {
        this.headers = headers;
    }

    public boolean isDisableComments() {
        return disableComments;
    }

    public void setDisableComments(boolean disableComments) {
        this.disableComments = disableComments;
    }

    public boolean isAnonymousCreator() {
        return anonymousCreator;
    }

    public void setAnonymousCreator(boolean anonymousCreator) {
        this.anonymousCreator = anonymousCreator;
    }

    public long getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(long viewsCount) {
        this.viewsCount = viewsCount;
    }

    public Integer getCountryId() {
        return countryId;
    }

    public void setCountryId(Integer countryId) {
        this.countryId = countryId;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Collection<Profession> getProfessions() {
        return professions;
    }

    public void setProfessions(Collection<Profession> professions) {
        this.professions = professions;
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

    public enum PublishStatus {

        NOT_PUBLISHED(0),

        PUBLISHED(1);

        private final int code;

        PublishStatus(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static PublishStatus fromCode(Integer code) {
            for (PublishStatus status: values()) {
                if (Objects.equals(code, status.code)) {
                    return status;
                }
            }

            return null;
        }
    }
}
