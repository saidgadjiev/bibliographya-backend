package ru.saidgadjiev.bibliographya.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.saidgadjiev.bibliographya.configuration.BibliographyaConfiguration;
import ru.saidgadjiev.bibliographya.domain.BiographyCategory;
import ru.saidgadjiev.bibliographya.domain.Country;
import ru.saidgadjiev.bibliographya.domain.Profession;
import ru.saidgadjiev.bibliographya.html.Header;

import java.sql.Timestamp;
import java.util.Collection;

public class BiographyBaseResponse {

    public static final String DISABLE_COMMENTS = "disableComments";

    public static final String ANONYMOUS_CREATOR = "anonymousCreator";

    public static final String CREATOR_ID = "creatorId";

    public static final String MODERATION_STATUS = "moderationStatus";

    private Integer id;

    private String firstName;

    private String lastName;

    private String middleName;

    private String bio;

    private Integer creatorId;

    private Integer userId;

    private int likesCount;

    private long commentsCount;

    private long viewsCount;

    private boolean liked;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BibliographyaConfiguration.DATE_FORMAT)
    private Timestamp updatedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BibliographyaConfiguration.DATE_FORMAT)
    private Timestamp createdAt;

    private ShortBiographyResponse creator;

    private Collection<BiographyCategory> categories;

    private Collection<Profession> professions;

    private Integer publishStatus;

    private Collection<Header> headers;

    private boolean disableComments;

    private boolean anonymousCreator;

    private Integer moderationStatus;

    private Integer countryId;

    private Country country;

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

    public Integer getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(Integer moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
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
}
