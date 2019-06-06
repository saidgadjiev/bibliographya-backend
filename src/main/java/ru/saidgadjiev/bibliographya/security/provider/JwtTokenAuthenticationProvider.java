package ru.saidgadjiev.bibliographya.security.provider;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.security.cache.BibliographyaUserCache;
import ru.saidgadjiev.bibliographya.service.api.BibliographyaUserDetailsService;

public class JwtTokenAuthenticationProvider implements AuthenticationProvider {

    private BibliographyaUserDetailsService userDetailsService;

    private BibliographyaUserCache bibliographyaUserCache;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Integer userId = (Integer) authentication.getPrincipal();

        if (userId == null) {
            throw new BadCredentialsException("Bad credentials");
        }
        ProviderType providerType = ProviderType.fromId((String) authentication.getCredentials());

        if (providerType == null) {
            throw new BadCredentialsException("Bad credentials");
        }

        User user = bibliographyaUserCache.getUserFromCache(userId);

        if (user == null) {
            switch (providerType) {
                case FACEBOOK:
                case VK:
                    user = userDetailsService.loadSocialUserById(userId);
                    break;
                case SIMPLE:
                    user = userDetailsService.loadUserById(userId);
                    break;
            }
            if (user == null) {
                throw new BadCredentialsException("Bad credentials");
            }

            bibliographyaUserCache.putUserInCache(user);
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

    public void setUserCache(BibliographyaUserCache userCache) {
        this.bibliographyaUserCache = userCache;
    }
}
