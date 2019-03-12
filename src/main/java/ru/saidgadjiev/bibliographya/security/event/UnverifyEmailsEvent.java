package ru.saidgadjiev.bibliographya.security.event;

public class UnverifyEmailsEvent {

    private String email;

    public UnverifyEmailsEvent(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
