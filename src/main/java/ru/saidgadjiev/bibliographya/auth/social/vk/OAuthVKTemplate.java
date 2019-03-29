package ru.saidgadjiev.bibliographya.auth.social.vk;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.saidgadjiev.bibliographya.auth.social.OAuthTemplate;
import ru.saidgadjiev.bibliographya.auth.social.ResponseType;

/**
 * Created by said on 29.12.2018.
 */
public class OAuthVKTemplate extends OAuthTemplate {

    private static final String API_VERSION = "5.92";

    private static final String OAUTH_URL = "https://oauth.vk.com/authorize";

    private static final String ACCESS_TOKEN_URL = "https://oauth.vk.com/access_token";

    public OAuthVKTemplate(String clientId,
                           String clientSecret) {
        super(clientId, clientSecret, OAUTH_URL, ACCESS_TOKEN_URL);
    }

    @Override
    public String buildOAuthUrl(String redirectUri, ResponseType responseType, MultiValueMap<String, String> parameters) {
        if (parameters == null) {
            parameters = new LinkedMultiValueMap<>();
        }

        parameters.set("v", API_VERSION);

        return super.buildOAuthUrl(redirectUri, responseType, parameters);
    }
}
