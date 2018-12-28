package ru.saidgadjiev.bibliography.service.impl.auth.social;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.OAuth2Operations;
import org.springframework.social.oauth2.OAuth2Parameters;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import ru.saidgadjiev.bibliography.auth.ProviderType;
import ru.saidgadjiev.bibliography.auth.SocialUserInfo;
import ru.saidgadjiev.bibliography.properties.FacebookProperties;

/**
 * Created by said on 23.12.2018.
 */
@Service
public class FacebookService {

    private static final String ID = "facebook";

    private final ConnectionFactoryLocator connectionFactoryLocator;

    private final FacebookProperties facebookProperties;

    @Autowired
    public FacebookService(ConnectionFactoryLocator connectionFactoryLocator, FacebookProperties facebookProperties) {
        this.connectionFactoryLocator = connectionFactoryLocator;
        this.facebookProperties = facebookProperties;
    }

    public String createFacebookAuthorizationUrl() {
        FacebookConnectionFactory connectionFactory = (FacebookConnectionFactory) connectionFactoryLocator.getConnectionFactory(ID);

        OAuth2Operations oauthOperations = connectionFactory.getOAuthOperations();
        OAuth2Parameters params = new OAuth2Parameters();
        params.setRedirectUri("http://localhost:8080/facebook/callback");
        params.setScope("public_profile");

        return oauthOperations.buildAuthorizeUrl(params);
    }

    public AccessGrant createFacebookAccessToken(String code) {
        FacebookConnectionFactory connectionFactory = (FacebookConnectionFactory) connectionFactoryLocator.getConnectionFactory(ID);

        return connectionFactory.getOAuthOperations().exchangeForAccess(
                code,
                "http://localhost:8080/facebook/callback",
                null
        );
    }

    public SocialUserInfo getUserInfo(AccessGrant accessGrant) {
        FacebookConnectionFactory connectionFactory = (FacebookConnectionFactory) connectionFactoryLocator.getConnectionFactory(ID);

        Facebook facebook = connectionFactory.createConnection(accessGrant).getApi();

        ObjectNode objectNode = facebook.fetchObject("me", ObjectNode.class, "id", "first_name", "last_name", "middle_name");

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
        FacebookConnectionFactory connectionFactory = (FacebookConnectionFactory) connectionFactoryLocator.getConnectionFactory(ID);

        Facebook facebook = connectionFactory.createConnection(accessGrant).getApi();

        ObjectNode objectNode = facebook.fetchObject("debug_token", ObjectNode.class, new LinkedMultiValueMap<String, String>() {{
            add("input_token", accessGrant.getAccessToken());
            add("access_token", accessGrant.getAccessToken());
        }});

        JsonNode data = objectNode.get("data");

        TokenInfo tokenInfo = new TokenInfo();

        tokenInfo.setValid(data.get("is_valid").asBoolean());

        return tokenInfo;
    }
}
