package ru.saidgadjiev.bibliographya.service.impl.auth.social;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.auth.social.SocialUserInfo;
import ru.saidgadjiev.bibliographya.auth.social.TokenInfo;
import ru.saidgadjiev.bibliographya.properties.VKProperties;
import ru.saidgadjiev.bibliographya.auth.social.AccessGrant;
import ru.saidgadjiev.bibliographya.auth.social.vk.OAuthVKTemplate;
import ru.saidgadjiev.bibliographya.auth.social.vk.UserProfileOperations;
import ru.saidgadjiev.bibliographya.auth.social.vk.VK;

/**
 * Created by said on 29.12.2018.
 */
@Service
public class VKService {

    private final OAuthVKTemplate oAuthTemplate;

    public VKService(VKProperties vkProperties) {
        oAuthTemplate = new OAuthVKTemplate(
                vkProperties.getAppId(),
                vkProperties.getAppSecret(),
                vkProperties.getAppToken()
        );
    }

    public String createVKAuthorizationUrl() {
        return oAuthTemplate.buildOAuthUrl("http://localhost:8080/vk/callback", null);
    }

    public AccessGrant createFacebookAccessToken(String code) {
        return oAuthTemplate.exchangeForAccess(
                code,
                "http://localhost:8080/vk/callback",
                null
        );
    }

    public SocialUserInfo getUserInfo(String userId, String accessToken) {
        VK vk = new VK(accessToken);
        UserProfileOperations userProfileOperations = vk.getUserProfileOperations();

        ObjectNode objectNode = userProfileOperations.getFiels(
                userId, "id, first_name", "last_name"
        ).getBody();

        JsonNode response = objectNode.get("response");

        JsonNode userInfoNode = response.iterator().next();
        SocialUserInfo userInfo = new SocialUserInfo();

        userInfo.setId(userInfoNode.get("id").asText());
        userInfo.setFirstName(userInfoNode.get("first_name").asText());
        userInfo.setLastName(userInfoNode.get("last_name").asText());
        userInfo.setProviderId(ProviderType.VK.getId());

        return userInfo;
    }

    public TokenInfo checkToken(AccessGrant accessGrant) {
        return oAuthTemplate.checkToken(accessGrant.getAccessToken());
    }
}
