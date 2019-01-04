package ru.saidgadjiev.bibliography.service.impl;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * Created by said on 28.10.2018.
 */
@Service
public class SecurityService {

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

    public Authentication authenticate(Object principal, Collection<? extends GrantedAuthority> authorities) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return authentication;
    }

    public Authentication findLoggedInUserAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
