package ru.saidgadjiev.bibliographya.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.saidgadjiev.bibliographya.configuration.BibliographyaConfiguration;

import java.sql.Timestamp;

/**
 * Created by said on 18.11.2018.
 */
public class BiographyCommentResponse {

    private int id;

    private String content;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BibliographyaConfiguration.DATE_FORMAT)
    private Timestamp createdAt;

    private int biographyId;

    private Integer userId;

    private Integer parentId;

    private ShortBiographyResponse user;

    private ShortBiographyResponse parentUser;

    private boolean parentDeleted;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBiographyId() {
        return biographyId;
    }

    public void setBiographyId(int biographyId) {
        this.biographyId = biographyId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public ShortBiographyResponse getUser() {
        return user;
    }

    public void setUser(ShortBiographyResponse user) {
        this.user = user;
    }

    public ShortBiographyResponse getParentUser() {
        return parentUser;
    }

    public void setParentUser(ShortBiographyResponse parentUser) {
        this.parentUser = parentUser;
    }

    public boolean isParentDeleted() {
        return parentDeleted;
    }

    public void setParentDeleted(boolean parentDeleted) {
        this.parentDeleted = parentDeleted;
    }
}
