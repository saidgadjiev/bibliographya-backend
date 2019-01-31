package ru.saidgadjiev.bibliographya.model;

import ru.saidgadjiev.bibliographya.bussiness.bug.BugAction;

import java.util.Collection;

public class BugResponse {

    private Integer id;

    private String theme;

    private String bugCase;

    private Integer userId;

    private ShortBiographyResponse user;

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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public ShortBiographyResponse getUser() {
        return user;
    }

    public void setUser(ShortBiographyResponse user) {
        this.user = user;
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
