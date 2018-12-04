package ru.saidgadjiev.bibliography.model;

/**
 * Created by said on 18.11.2018.
 */
public class BiographyCommentRequest {

    private Integer id;

    private String content;

    private String firstName;

    private String lastName;

    private BiographyCommentRequest parent;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public BiographyCommentRequest getParent() {
        return parent;
    }

    public void setParent(BiographyCommentRequest parent) {
        this.parent = parent;
    }
}
