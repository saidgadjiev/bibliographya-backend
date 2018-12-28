package ru.saidgadjiev.bibliography.service.impl.auth;

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
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliography.auth.ProviderType;
import ru.saidgadjiev.bibliography.auth.SocialUserInfo;
import ru.saidgadjiev.bibliography.domain.User;
import ru.saidgadjiev.bibliography.model.SignUpRequest;
import ru.saidgadjiev.bibliography.security.service.SecurityService;
import ru.saidgadjiev.bibliography.service.api.UserService;
import ru.saidgadjiev.bibliography.service.impl.TokenCookieService;
import ru.saidgadjiev.bibliography.service.impl.TokenService;
import ru.saidgadjiev.bibliography.service.impl.auth.social.FacebookService;
import ru.saidgadjiev.bibliography.service.impl.auth.social.SocialUserDetailsService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by said on 24.12.2018.
 */
@Service
public class AuthService {

    private final FacebookService facebookService;

    private final SocialUserDetailsService socialUserDetailsService;

    private final UserService userService;

    private final TokenService tokenService;

    private final TokenCookieService tokenCookieService;

    private final SecurityService securityService;

    private AuthenticationManager authenticationManager;

    private LogoutHandler logoutHandler = new CompositeLogoutHandler(
            new CookieClearingLogoutHandler("X-TOKEN"),
            new SecurityContextLogoutHandler()
    );

    @Autowired
    public AuthService(FacebookService facebookService,
                       SocialUserDetailsService socialUserDetailsService,
                       UserService userService,
                       TokenService tokenService,
                       TokenCookieService tokenCookieService,
                       SecurityService securityService) {
        this.facebookService = facebookService;
        this.socialUserDetailsService = socialUserDetailsService;
        this.userService = userService;
        this.tokenService = tokenService;
        this.tokenCookieService = tokenCookieService;
        this.securityService = securityService;
    }

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public String getOauthUrl(ProviderType providerType) {
        switch (providerType) {
            case FACEBOOK:
                return facebookService.createFacebookAuthorizationUrl();
        }

        return null;
    }

    public User auth(AuthContext authContext) throws SQLException {
        User user = null;
        AccessGrant accessGrant = null;

        switch (authContext.getProviderType()) {
            case FACEBOOK:
                accessGrant = facebookService.createFacebookAccessToken(authContext.getCode());

                SocialUserInfo userInfo = facebookService.getUserInfo(accessGrant);

                user = (User) socialUserDetailsService.loadSocialUserByAccountId(ProviderType.FACEBOOK, userInfo.getId());

                if (user == null) {
                    user = (User) socialUserDetailsService.saveSocialUser(userInfo);
                }

                securityService.authenticate(user, user.getAuthorities());

                break;
            case USERNAME_PASSWORD:
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                authContext.getSignInRequest().getUsername(),
                                authContext.getSignInRequest().getPassword()
                        );
                Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

                securityService.authenticate(authentication.getPrincipal(), authentication.getAuthorities());

                break;
        }

        String token = tokenService.createToken(user, accessGrant);

        tokenCookieService.addCookie(authContext.getResponse(), "X-TOKEN", token);

        return user;
    }

    public void signUp(SignUpRequest signUpRequest) throws SQLException {
        userService.save(signUpRequest);
    }

    public User signOut(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = securityService.findLoggedInUserAuthentication();

        logoutHandler.logout(request, response, authentication);

        if (authentication == null) {
            return null;
        }

        return (User) authentication.getPrincipal();
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
                case FACEBOOK:
                    userDetails = socialUserDetailsService.loadSocialUserById(userId);
                    break;
                case USERNAME_PASSWORD:
                    userDetails = userService.loadUserById(userId);
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
