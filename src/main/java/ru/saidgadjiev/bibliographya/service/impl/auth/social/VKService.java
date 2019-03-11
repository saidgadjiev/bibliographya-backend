package ru.saidgadjiev.bibliographya.service.impl.auth.social;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.auth.social.AccessGrant;
import ru.saidgadjiev.bibliographya.auth.social.SocialUserInfo;
import ru.saidgadjiev.bibliographya.auth.social.vk.OAuthVKTemplate;
import ru.saidgadjiev.bibliographya.auth.social.vk.UserProfileOperations;
import ru.saidgadjiev.bibliographya.auth.social.vk.VK;
import ru.saidgadjiev.bibliographya.properties.VKProperties;
import ru.saidgadjiev.bibliographya.service.api.SocialService;

/**
 * Created by said on 29.12.2018.
 */
@Service
public class VKService implements SocialService {

    private final OAuthVKTemplate oAuthTemplate;

    public VKService(VKProperties vkProperties) {
        oAuthTemplate = new OAuthVKTemplate(
                vkProperties.getAppId(),
                vkProperties.getAppSecret()
        );
    }

    @Override
    public String createOAuth2Url(String redirectUri) {
        return oAuthTemplate.buildOAuthUrl(redirectUri, null);
    }

    @Override
    public AccessGrant createAccessToken(String code, String redirectUri) {
        return oAuthTemplate.exchangeForAccess(
                code,
                redirectUri,
                null
        );
    }

    @Override
    public SocialUserInfo getUserInfo(String userId, String accessToken) {
        VK vk = new VK(accessToken);
        UserProfileOperations userProfileOperations = vk.getUserProfileOperations();

        ObjectNode objectNode = userProfileOperations.getFields(
                userId, "id", "first_name", "last_name"
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
}
