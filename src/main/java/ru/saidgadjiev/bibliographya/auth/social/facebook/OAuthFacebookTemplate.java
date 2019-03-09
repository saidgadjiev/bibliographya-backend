package ru.saidgadjiev.bibliographya.auth.social.facebook;

import ru.saidgadjiev.bibliographya.auth.social.OAuthTemplate;

/**
 * Created by said on 29.12.2018.
 */
public class OAuthFacebookTemplate extends OAuthTemplate {

    private static final String API_VERSION = "3.2";

    private static final String GRAPH_API_URL = "https://graph.facebook.com/v" + API_VERSION + "/";

    private static final String OAUTH_URL = "https://www.facebook.com/v" + API_VERSION + "/dialog/oauth";

    private static final String ACCESS_TOKEN_URL = GRAPH_API_URL + "oauth/access_token";

    public OAuthFacebookTemplate(String clientId,
                                 String clientSecret) {
        super(clientId, clientSecret, OAUTH_URL, ACCESS_TOKEN_URL);
    }
}
