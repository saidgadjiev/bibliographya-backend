package ru.saidgadjiev.bibliographya.security.event;

import ru.saidgadjiev.bibliographya.domain.User;

public class ChangePhoneEvent {

    private User user;

    public ChangePhoneEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
