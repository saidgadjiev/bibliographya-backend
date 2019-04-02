package ru.saidgadjiev.bibliographya.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.saidgadjiev.bibliographya.configuration.BibliographyaConfiguration;
import ru.saidgadjiev.bibliographya.domain.BiographyCategory;
import ru.saidgadjiev.bibliographya.html.Header;

import java.sql.Timestamp;
import java.util.Collection;

public class BiographyBaseResponse {

    public static final String ONLY_IN_CATEGORY = "onlyInCategory";

    public static final String DISABLE_COMMENTS = "disableComments";

    public static final String ANONYMOUS_CREATOR = "anonymousCreator";

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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BibliographyaConfiguration.DATE_FORMAT)
    private Timestamp updatedAt;

    private ShortBiographyResponse creator;

    private Collection<BiographyCategory> categories;

    private Integer publishStatus;

    private Collection<Header> headers;

    private boolean onlyInCategory;

    private boolean disableComments;

    private boolean anonymousCreator;

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
        this.updatedAt = updatedAt;
    }

    public ShortBiographyResponse getCreator() {
        return creator;
    }

    public void setCreator(ShortBiographyResponse creator) {
        this.creator = creator;
    }

    public Collection<BiographyCategory> getCategories() {
        return categories;
    }

    public void setCategories(Collection<BiographyCategory> categories) {
        this.categories = categories;
    }

    public Integer getPublishStatus() {
        return publishStatus;
    }

    public void setPublishStatus(Integer publishStatus) {
        this.publishStatus = publishStatus;
    }

    public Collection<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(Collection<Header> headers) {
        this.headers = headers;
    }

    public boolean isOnlyInCategory() {
        return onlyInCategory;
    }

    public void setOnlyInCategory(boolean onlyInCategory) {
        this.onlyInCategory = onlyInCategory;
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
}
