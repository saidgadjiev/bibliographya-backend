package ru.saidgadjiev.bibliography.domain;

/**
 * Created by said on 25.12.2018.
 */
public class SocialAccount {

    private int id;

    private String accountId;

    private int userId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
