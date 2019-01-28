package ru.saidgadjiev.bibliographya.service.impl.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.CompositeLogoutHandler;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;
import ru.saidgadjiev.bibliographya.auth.common.AuthContext;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.auth.social.AccessGrant;
import ru.saidgadjiev.bibliographya.auth.social.SocialUserInfo;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.model.SignUpRequest;
import ru.saidgadjiev.bibliographya.service.api.BibliographyaUserDetailsService;
import ru.saidgadjiev.bibliographya.service.impl.SecurityService;
import ru.saidgadjiev.bibliographya.service.impl.TokenCookieService;
import ru.saidgadjiev.bibliographya.service.impl.TokenService;
import ru.saidgadjiev.bibliographya.service.impl.auth.social.FacebookService;
import ru.saidgadjiev.bibliographya.service.impl.auth.social.VKService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by said on 24.12.2018.
 */
@Service
public class AuthService {

    private FacebookService facebookService;

    private VKService vkService;

    private BibliographyaUserDetailsService userAccountDetailsService;

    private TokenService tokenService;

    private TokenCookieService tokenCookieService;

    private SecurityService securityService;

    private AuthenticationManager authenticationManager;

    private LogoutHandler logoutHandler = new CompositeLogoutHandler(
            new CookieClearingLogoutHandler("X-TOKEN"),
            new SecurityContextLogoutHandler()
    );


    @Autowired
    public void setFacebookService(FacebookService facebookService) {
        this.facebookService = facebookService;
    }

    @Autowired
    public void setVkService(VKService vkService) {
        this.vkService = vkService;
    }

    @Autowired
    public void setUserAccountDetailsService(BibliographyaUserDetailsService userAccountDetailsService) {
        this.userAccountDetailsService = userAccountDetailsService;
    }

    @Autowired
    public void setTokenService(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Autowired
    public void setTokenCookieService(TokenCookieService tokenCookieService) {
        this.tokenCookieService = tokenCookieService;
    }

    @Autowired
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public String getOauthUrl(ProviderType providerType, String redirectUri) {
        switch (providerType) {
            case FACEBOOK:
                return facebookService.createFacebookAuthorizationUrl(redirectUri);
            case VK:
                return vkService.createVKAuthorizationUrl(redirectUri);
            case USERNAME_PASSWORD:
                break;
        }

        return null;
    }

    public User auth(AuthContext authContext, String redirectUri) throws SQLException {
        User user = null;
        AccessGrant accessGrant = null;

        switch (authContext.getProviderType()) {
            case FACEBOOK: {
                accessGrant = facebookService.createFacebookAccessToken(authContext.getCode(), redirectUri);

                SocialUserInfo userInfo = facebookService.getUserInfo(accessGrant.getAccessToken());

                user = (User) userAccountDetailsService.loadSocialUserByAccountId(ProviderType.FACEBOOK, userInfo.getId());

                if (user == null) {
                    user = (User) userAccountDetailsService.saveSocialUser(userInfo);

                    user.setIsNew(true);
                }

                break;
            }
            case VK: {
                accessGrant = vkService.createAccessToken(authContext.getCode(), redirectUri);

                SocialUserInfo userInfo = vkService.getUserInfo(accessGrant.getUserId(), accessGrant.getAccessToken());

                user = (User) userAccountDetailsService.loadSocialUserByAccountId(ProviderType.VK, userInfo.getId());

                if (user == null) {
                    user = (User) userAccountDetailsService.saveSocialUser(userInfo);

                    user.setIsNew(true);
                }

                break;
            }
            case USERNAME_PASSWORD:
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                authContext.getSignInRequest().getUsername(),
                                authContext.getSignInRequest().getPassword()
                        );
                Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

                user = (User) authentication.getPrincipal();

                break;
        }

        securityService.authenticate(user, user.getAuthorities());

        String token = tokenService.createToken(user, accessGrant);

        tokenCookieService.addCookie(authContext.getResponse(), "X-TOKEN", token);

        return user;
    }

    public void signUp(SignUpRequest signUpRequest) throws SQLException {
        userAccountDetailsService.save(signUpRequest);
    }

    public User signOut(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = securityService.findLoggedInUserAuthentication();

        if (authentication != null) {
            User user = (User) authentication.getPrincipal();

            logoutHandler.logout(request, response, authentication);

            return user;
        }

        return null;
    }

    public UserDetails account() {
        return securityService.findLoggedInUser();
    }

    public void tokenAuth(String token) {
        Map<String, Object> details = tokenService.validate(token);

        if (details == null) {
            SecurityContextHolder.getContext().setAuthentication(null);
        } else {
            ProviderType providerType = ProviderType.fromId((String) details.get("providerId"));
            Integer userId = (Integer) details.get("userId");
            UserDetails userDetails = null;

            switch (providerType) {
                case VK:
                case FACEBOOK:
                    userDetails = userAccountDetailsService.loadSocialUserById(userId);
                    break;
                case USERNAME_PASSWORD:
                    userDetails = userAccountDetailsService.loadUserById(userId);
                    break;
            }

            if (userDetails != null) {
                ((CredentialsContainer) userDetails).eraseCredentials();

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } else {
                SecurityContextHolder.getContext().setAuthentication(null);
            }
        }
    }
}
