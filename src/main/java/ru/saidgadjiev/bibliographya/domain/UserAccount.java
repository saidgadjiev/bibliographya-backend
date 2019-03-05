package ru.saidgadjiev.bibliographya.domain;

/**
 * Created by said on 25.12.2018.
 */
public class UserAccount {

    public static final String ID = "id";

    public static final String EMAIL = "email";

    public static final String EMAIL_VERIFIED = "email_verified";

    public static final String PASSWORD = "password";

    public static final String USER_ID = "user_id";

    private int id;

    private String email;

    private boolean emailVerified;

    private String password;

    private int userId;

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

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
