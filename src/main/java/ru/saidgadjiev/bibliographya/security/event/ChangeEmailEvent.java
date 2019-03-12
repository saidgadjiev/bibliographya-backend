package ru.saidgadjiev.bibliographya.security.event;

import ru.saidgadjiev.bibliographya.domain.User;

public class ChangeEmailEvent {

    private User user;

    public ChangeEmailEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
