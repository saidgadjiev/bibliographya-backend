package ru.saidgadjiev.bibliographya.security.provider;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.service.api.BibliographyaUserDetailsService;

public class JwtTokenAuthenticationProvider implements AuthenticationProvider {

    private BibliographyaUserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Integer userId = (Integer) authentication.getPrincipal();

        if (userId == null) {
            throw new BadCredentialsException("Bad credentials");
        }

        User user = userDetailsService.loadUserById(userId);

        if (user == null) {
            throw new BadCredentialsException("Bad credentials");
        }

        return new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public void setUserDetailsService(BibliographyaUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
}
