package ru.saidgadjiev.bibliographya.model;

import java.sql.Timestamp;

/**
 * Created by said on 18.11.2018.
 */
public class BiographyCommentResponse {

    private int id;

    private String content;

    private String firstName;

    private String lastName;

    private Integer userId;

    private String replyToFirstName;

    private Integer replyToUserId;

    private Integer parentId;

    private Timestamp createdAt;

    private int biographyId;

    private int replyToBiographyId;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Integer getUserName() {
        return userId;
    }

    public void setUserName(Integer userId) {
        this.userId = userId;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getReplyToFirstName() {
        return replyToFirstName;
    }

    public void setReplyToFirstName(String replyToFirstName) {
        this.replyToFirstName = replyToFirstName;
    }

    public Integer getReplyToUserId() {
        return replyToUserId;
    }

    public void setReplyToUserId(Integer replyToUserId) {
        this.replyToUserId = replyToUserId;
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

    public int getReplyToBiographyId() {
        return replyToBiographyId;
    }

    public void setReplyToBiographyId(int replyToBiographyId) {
        this.replyToBiographyId = replyToBiographyId;
    }
}
