package ru.saidgadjiev.bibliographya.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class BiographyCategoryRequest {

    @NotNull
    @Size(min = 1)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
