package ru.saidgadjiev.bibliographya.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by said on 18.11.2018.
 */
public class BiographyCommentRequest {

    private Integer id;

    @NotNull
    @Size(min = 1)
    private String content;

    private String firstName;

    private int biographyId;

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

    public BiographyCommentRequest getParent() {
        return parent;
    }

    public void setParent(BiographyCommentRequest parent) {
        this.parent = parent;
    }

    public int getBiographyId() {
        return biographyId;
    }

    public void setBiographyId(int biographyId) {
        this.biographyId = biographyId;
    }
}
