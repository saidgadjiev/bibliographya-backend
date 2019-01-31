package ru.saidgadjiev.bibliographya.domain;

public class Bug {

    private Integer id;

    private String theme;

    private String bugCase;

    private Integer userId;

    private Biography user;

    private BugStatus status;

    private String info;

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

    public BugStatus getStatus() {
        return status;
    }

    public void setStatus(BugStatus status) {
        this.status = status;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Biography getUser() {
        return user;
    }

    public void setUser(Biography user) {
        this.user = user;
    }

    public enum BugStatus {

        OPENED(0),
        CLOSED(1),
        IGNORED(2);

        private final int code;

        BugStatus(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
