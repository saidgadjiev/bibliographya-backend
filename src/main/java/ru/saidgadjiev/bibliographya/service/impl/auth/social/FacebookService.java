package ru.saidgadjiev.bibliographya.service.impl.auth.social;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.auth.social.AccessGrant;
import ru.saidgadjiev.bibliographya.auth.social.SocialUserInfo;
import ru.saidgadjiev.bibliographya.auth.social.TokenInfo;
import ru.saidgadjiev.bibliographya.auth.social.facebook.Facebook;
import ru.saidgadjiev.bibliographya.auth.social.facebook.OAuthFacebookTemplate;
import ru.saidgadjiev.bibliographya.auth.social.facebook.PermissionOperations;
import ru.saidgadjiev.bibliographya.auth.social.facebook.UserProfileOperations;
import ru.saidgadjiev.bibliographya.properties.FacebookProperties;


/**
 * Created by said on 23.12.2018.
 */
@Service
public class FacebookService {

    private final OAuthFacebookTemplate oAuthTemplate;

    @Autowired
    public FacebookService(FacebookProperties facebookProperties) {
        oAuthTemplate = new OAuthFacebookTemplate(
                facebookProperties.getAppId(),
                facebookProperties.getAppSecret(),
                facebookProperties.getAppToken()
        );
    }

    public String createFacebookAuthorizationUrl(String redirectUri) {
        return oAuthTemplate.buildOAuthUrl(redirectUri, null);
    }

    public AccessGrant createFacebookAccessToken(String code, String redirectUri) {
        return oAuthTemplate.exchangeForAccess(
                code,
                redirectUri,
                null
        );
    }

    public SocialUserInfo getUserInfo(String accessToken) {
        Facebook facebook = new Facebook(accessToken);
        UserProfileOperations userProfileOperations = facebook.getUserProfileOperations();

        ObjectNode objectNode = userProfileOperations.getFiels(
                "id", "first_name", "last_name", "middle_name"
        ).getBody();

        SocialUserInfo userInfo = new SocialUserInfo();

        userInfo.setId(objectNode.get("id").asText());
        userInfo.setFirstName(objectNode.get("first_name").asText());
        userInfo.setLastName(objectNode.get("last_name").asText());
        userInfo.setProviderId(ProviderType.FACEBOOK.getId());

        if (objectNode.has("middle_name")) {
            userInfo.setMiddleName(objectNode.get("middle_name").asText());
        }

        return userInfo;
    }

    public TokenInfo checkToken(AccessGrant accessGrant) {
        return oAuthTemplate.checkToken(accessGrant.getAccessToken());
    }

    public void logout(String userId, String accessToken) {
        Facebook facebook = new Facebook(accessToken);
        PermissionOperations permissionOperations = facebook.getPermissionOperations();

        permissionOperations.deletePermissions(userId);
    }
}
