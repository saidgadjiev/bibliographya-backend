package ru.saidgadjiev.bibliographya.security.provider;

import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.saidgadjiev.bibliographya.domain.AuthKey;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.service.api.BibliographyaUserDetailsService;

import java.util.ArrayList;
import java.util.List;

public class CustomAuthenticationProvider implements AuthenticationProvider {

    private BibliographyaUserDetailsService userDetailsService;

    private MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    private PasswordEncoder passwordEncoder;

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    public void setUserDetailsService(BibliographyaUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        AuthKey authKey = (AuthKey) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();

        List<User> users = userDetailsService.loadUserByUsername(authKey);
        List<User> found = new ArrayList<>();

        for (User user: users) {
            if (passwordEncoder.matches(password, user.getPassword())) {
                found.add(user);
            }
        }

        if (found.isEmpty()) {
            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "Bad credentials"));
        }

        User toAuthenticate;

        if (found.size() == 1) {
            toAuthenticate = found.get(0);
        } else {
            toAuthenticate = found.stream().filter(user -> {
                switch (authKey.getType()) {
                    case PHONE:
                        return user.isPhoneVerified();
                    case EMAIL:
                        return user.isEmailVerified();
                }

                return false;
            }).findAny().orElseThrow(() -> new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "Bad credentials")));
        }

        return new UsernamePasswordAuthenticationToken(
                toAuthenticate, authentication.getCredentials(),
                authoritiesMapper.mapAuthorities(toAuthenticate.getAuthorities()));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
