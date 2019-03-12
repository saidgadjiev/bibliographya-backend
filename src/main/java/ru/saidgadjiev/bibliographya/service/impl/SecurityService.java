package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Created by said on 28.10.2018.
 */
@Service
public class SecurityService {

    private ApplicationEventPublisher eventPublisher;

    @Autowired
    public SecurityService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public UserDetails findLoggedInUser() {
        Authentication authentication = findLoggedInUserAuthentication();

        if (authentication == null) {
            return null;
        }
        Object userDetails = authentication.getPrincipal();

        if (userDetails == null) {
            return null;
        }

        if (userDetails instanceof UserDetails) {
            return (UserDetails) userDetails;
        }

        return null;
    }

    public Authentication autoLogin(UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        eventPublisher.publishEvent(new AuthenticationSuccessEvent(authentication));

        return authentication;
    }

    public Authentication findLoggedInUserAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
