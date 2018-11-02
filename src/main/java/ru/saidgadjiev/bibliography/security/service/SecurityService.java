package ru.saidgadjiev.bibliography.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Created by said on 28.10.2018.
 */
@Service
public class SecurityService {

    private final AuthenticationManager authenticationManager;

    @Autowired
    public SecurityService(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
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

    public Authentication signIn(String username, String password) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return authentication;
    }

    public Authentication findLoggedInUserAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
