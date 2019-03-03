package ru.saidgadjiev.bibliographya.domain;

/**
 * Created by said on 03.03.2019.
 */
public class Email {

    private String email;

    private boolean verified;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
