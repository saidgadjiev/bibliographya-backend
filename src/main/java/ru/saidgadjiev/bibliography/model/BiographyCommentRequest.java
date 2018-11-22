package ru.saidgadjiev.bibliography.model;

/**
 * Created by said on 18.11.2018.
 */
public class BiographyCommentRequest {

    private String content;

    private Integer parentId;

    private String firstName;

    private String lastName;

    private String replyToFirstName;

    private String replyToUserName;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
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

    public String getReplyToFirstName() {
        return replyToFirstName;
    }

    public void setReplyToFirstName(String replyToFirstName) {
        this.replyToFirstName = replyToFirstName;
    }

    public String getReplyToUserName() {
        return replyToUserName;
    }

    public void setReplyToUserName(String replyToUserName) {
        this.replyToUserName = replyToUserName;
    }
}
