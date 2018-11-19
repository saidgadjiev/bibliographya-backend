package ru.saidgadjiev.bibliography.model;

/**
 * Created by said on 18.11.2018.
 */
public class BiographyCommentRequest {

    private String content;

    private Integer parentId;

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
}
