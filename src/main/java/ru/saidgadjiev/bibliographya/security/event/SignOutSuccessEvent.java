package ru.saidgadjiev.bibliographya.security.event;

import org.springframework.security.core.Authentication;

public class SignOutSuccessEvent {

    private Authentication authentication;

    public SignOutSuccessEvent(Authentication authentication) {
        this.authentication = authentication;
    }

    public Authentication getAuthentication() {
        return authentication;
    }
}
