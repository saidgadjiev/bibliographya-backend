package ru.saidgadjiev.bibliographya.security.provider;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import ru.saidgadjiev.bibliographya.auth.common.AuthContext;
import ru.saidgadjiev.bibliographya.auth.social.AccessGrant;
import ru.saidgadjiev.bibliographya.auth.social.SocialUserInfo;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.factory.SocialServiceFactory;
import ru.saidgadjiev.bibliographya.service.api.BibliographyaUserDetailsService;
import ru.saidgadjiev.bibliographya.service.api.SocialService;

import java.sql.SQLException;

/**
 * Created by said on 09/05/2019.
 */
public class SocialAuthenticationProvider implements AuthenticationProvider {

    private BibliographyaUserDetailsService userDetailsService;

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    private SocialServiceFactory socialServiceFactory;

    public SocialAuthenticationProvider(SocialServiceFactory socialServiceFactory) {
        this.socialServiceFactory = socialServiceFactory;
    }

    public void setUserDetailsService(BibliographyaUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            AuthContext authContext = (AuthContext) authentication.getDetails();

            switch (authContext.getProviderType()) {
                case VK: {
                    SocialService socialService = socialServiceFactory.getService(authContext.getProviderType());
                    AccessGrant accessGrant = socialService.createAccessToken(authContext.getCode(), authContext.getRedirectUri());

                    SocialUserInfo userInfo = socialService.getUserInfo(accessGrant.getUserId(), accessGrant.getAccessToken());

                    User user = userDetailsService.loadUserBySocialAccount(authContext.getProviderType(), userInfo.getId());

                    if (user == null) {
                        user = userDetailsService.saveSocialUser(userInfo);

                        user.setIsNew(true);
                    }

                    return new UsernamePasswordAuthenticationToken(
                            user, authentication.getCredentials(),
                            authoritiesMapper.mapAuthorities(user.getAuthorities())
                    );
                }
                default:
                    throw new InternalAuthenticationServiceException("Provider " + authContext.getProviderType() + " not supported yet");
            }
        } catch (SQLException ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
