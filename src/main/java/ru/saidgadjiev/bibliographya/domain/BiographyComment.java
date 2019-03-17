package ru.saidgadjiev.bibliographya.domain;

import java.sql.Timestamp;

/**
 * Created by said on 16.11.2018.
 */
public class BiographyComment {

    public static final String TABLE = "biography_comment";

    public static final String ID = "id";

    public static final String PARENT_USER_ID = "parent_user_id";

    public static final String USER_ID = "user_id";

    private int id;

    private String content;

    private Timestamp createdAt;

    private int biographyId;

    private Integer userId;

    private Integer parentId;

    private Integer parentUserId;

    private Biography user;

    private Biography parentUser;

    private boolean parentDeleted;

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

    public Integer getParentUserId() {
        return parentUserId;
    }

    public void setParentUserId(Integer parentUserId) {
        this.parentUserId = parentUserId;
    }

    public Biography getParentUser() {
        return parentUser;
    }

    public void setParentUser(Biography parentUser) {
        this.parentUser = parentUser;
    }

    public boolean isParentDeleted() {
        return parentDeleted;
    }

    public void setParentDeleted(boolean parentDeleted) {
        this.parentDeleted = parentDeleted;
    }
}
