package ru.saidgadjiev.bibliographya.domain;

import java.sql.Timestamp;

/**
 * Created by said on 11.02.2019.
 */
public class EmailVerification {

    private int id;

    private String email;

    private int code;

    private Timestamp expiredAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Timestamp getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(Timestamp expiredAt) {
        this.expiredAt = expiredAt;
    }
}
