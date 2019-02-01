package ru.saidgadjiev.bibliographya.model;

import ru.saidgadjiev.bibliographya.bussiness.bug.BugAction;

import java.util.Collection;

public class BugResponse {

    private Integer id;

    private String theme;

    private String bugCase;

    private Integer fixerId;

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
}
