package ru.saidgadjiev.bibliographya.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ru.saidgadjiev.bibliographya.domain.jackson.TrimDeserializer;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by said on 18.11.2018.
 */
public class BiographyCommentRequest {

    @NotNull
    @Size(min = 1)
    @JsonDeserialize(using = TrimDeserializer.class)
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
