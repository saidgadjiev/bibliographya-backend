package ru.saidgadjiev.bibliographya.domain;

import java.sql.Timestamp;

public class Bug {

    private Integer id;

    private String theme;

    private String bugCase;

    private Integer fixerId;

    private Biography fixer;

    private BugStatus status;

    private Timestamp createdAt;

    private Timestamp fixedAt;

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

    public Integer getFixerId() {
        return fixerId;
    }

    public void setFixerId(Integer fixerId) {
        this.fixerId = fixerId;
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

    public Biography getFixer() {
        return fixer;
    }

    public void setFixer(Biography fixer) {
        this.fixer = fixer;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getFixedAt() {
        return fixedAt;
    }

    public void setFixedAt(Timestamp fixedAt) {
        this.fixedAt = fixedAt;
    }

    public enum BugStatus {

        PENDING(0),
        CLOSED(1),
        IGNORED(2);

        private final int code;

        BugStatus(int code) {
            this.code = code;
        }

        public static BugStatus fromCode(int status) {
            for (BugStatus bugStatus: values()) {
                if (bugStatus.getCode() == status) {
                    return bugStatus;
                }
            }

            return null;
        }

        public int getCode() {
            return code;
        }
    }
}
