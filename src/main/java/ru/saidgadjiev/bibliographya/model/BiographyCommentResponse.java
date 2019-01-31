package ru.saidgadjiev.bibliographya.model;

import ru.saidgadjiev.bibliographya.domain.BiographyComment;

import java.sql.Timestamp;

/**
 * Created by said on 18.11.2018.
 */
public class BiographyCommentResponse {

    private int id;

    private String content;

    private Timestamp createdAt;

    private int biographyId;

    private Integer userId;

    private Integer parentId;

    private ShortBiographyResponse user;

    private BiographyComment parent;

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

    public BiographyComment getParent() {
        return parent;
    }

    public void setParent(BiographyComment parent) {
        this.parent = parent;
    }
}
