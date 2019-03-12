package ru.saidgadjiev.bibliographya.security.event;

import ru.saidgadjiev.bibliographya.domain.Role;

public class DeleteRoleEvent {

    private Role role;

    private int userId;

    public DeleteRoleEvent(Role role, int userId) {
        this.role = role;
        this.userId = userId;
    }

    public Role getRole() {
        return role;
    }

    public int getUserId() {
        return userId;
    }
}
