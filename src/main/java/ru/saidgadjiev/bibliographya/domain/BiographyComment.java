package ru.saidgadjiev.bibliographya.domain;

import java.sql.Timestamp;

/**
 * Created by said on 16.11.2018.
 */
public class BiographyComment {

    public static final String TYPE = "biography_comment";

    private int id;

    private String content;

    private Timestamp createdAt;

    private int biographyId;

    private Integer userId;

    private Integer parentId;

    private BiographyComment parent;

    private Biography user;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Biography getUser() {
        return user;
    }

    public void setUser(Biography user) {
        this.user = user;
    }

    public BiographyComment getParent() {
        return parent;
    }

    public void setParent(BiographyComment parent) {
        this.parent = parent;
    }
}
