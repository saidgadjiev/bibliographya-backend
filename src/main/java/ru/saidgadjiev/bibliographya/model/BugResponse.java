package ru.saidgadjiev.bibliographya.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.saidgadjiev.bibliographya.bussiness.bug.BugAction;
import ru.saidgadjiev.bibliographya.configuration.BibliographyaConfiguration;

import java.sql.Timestamp;
import java.util.Collection;

public class BugResponse {

    private Integer id;

    private String theme;

    private String bugCase;

    private Integer fixerId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BibliographyaConfiguration.DATE_FORMAT)
    private Timestamp createdAt;

    private ShortBiographyResponse fixer;

    private int status;

    private String info;

    private Collection<BugAction> actions;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public Integer getFixerId() {
        return fixerId;
    }

    public void setFixerId(Integer fixerId) {
        this.fixerId = fixerId;
    }

    public ShortBiographyResponse getFixer() {
        return fixer;
    }

    public void setFixer(ShortBiographyResponse fixer) {
        this.fixer = fixer;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Collection<BugAction> getActions() {
        return actions;
    }

    public void setActions(Collection<BugAction> actions) {
        this.actions = actions;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
