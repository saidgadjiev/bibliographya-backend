package ru.saidgadjiev.bibliographya.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class BugRequest {

    @NotNull
    @Size(min = 1)
    private String theme;
    @NotNull
    @Size(min = 1)

    private String bugCase;

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getBugCase() {
        return bugCase;
    }

    public void setBugCase(String bugCase) {
        this.bugCase = bugCase;
    }
}
