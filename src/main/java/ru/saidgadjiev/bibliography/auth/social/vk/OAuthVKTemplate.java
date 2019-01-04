package ru.saidgadjiev.bibliography.auth.social.vk;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.saidgadjiev.bibliography.auth.social.TokenInfo;
import ru.saidgadjiev.bibliography.auth.social.AccessGrant;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by said on 29.12.2018.
 */
public class OAuthVKTemplate {

    private static final String API_VERSION = "5.92";

    private static final String VK_API_URL = "https://api.vk.com/method/";

    private static final String OAUTH_URL = "https://oauth.vk.com/authorize";

    private static final String TOKEN_CHECK_URL = VK_API_URL + "secure.checkToken";

    private static final String ACCESS_TOKEN_URL = "https://oauth.vk.com/access_token";

    private String clientId;

    private String clientSecret;

    private String appToken;

    private RestTemplate restTemplate;

    public OAuthVKTemplate(String clientId,
                           String clientSecret,
                           String appToken) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.appToken = appToken;
    }

    public String buildOAuthUrl(String redirectUri, MultiValueMap<String, String> parameters) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        if (parameters != null) {
            params.addAll(parameters);
        }

        params.set("response_type", "code");
        params.set("client_id", formEncode(clientId));
        params.set("redirect_uri", redirectUri);
        params.set("v", API_VERSION);

        return buildUrl(OAUTH_URL, params);
    }

    public AccessGrant exchangeForAccess(String code,
                                         String redirectUri,
                                         MultiValueMap<String, String> parameters) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        if (parameters != null) {
            params.addAll(parameters);
        }

        params.set("client_id", clientId);
        params.set("client_secret", clientSecret);
        params.set("code", code);
        params.set("redirect_uri", redirectUri);
        params.set("v", API_VERSION);

        String url = buildUrl(ACCESS_TOKEN_URL, params);

        ResponseEntity<ObjectNode> exchangeResponse = getRestTemplate().getForEntity(url, ObjectNode.class);
        ObjectNode body = exchangeResponse.getBody();

        return new AccessGrant(
                body.get("access_token").asText(),
                body.get("expires_in").asLong(),
                body.get("user_id").asText()
        );
    }

    public TokenInfo checkToken(String token) {
        String url = buildUrl(TOKEN_CHECK_URL, new LinkedMultiValueMap<String, String>() {{
            add("token", token);
        }});

        ResponseEntity<ObjectNode> checkTokenResponse = getRestTemplate().getForEntity(url, ObjectNode.class);
        ObjectNode body = checkTokenResponse.getBody();
        TokenInfo tokenInfo = new TokenInfo();

        tokenInfo.setValid(body.get("status").asInt() == 1);

        return tokenInfo;
    }

    private String buildUrl(String baseUrl, MultiValueMap<String, String> parameters) {
        StringBuilder url = new StringBuilder(baseUrl);

        if (parameters != null && !parameters.isEmpty()) {
            url.append('?');

            for (Iterator<Map.Entry<String, List<String>>> iterator = parameters.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, List<String>> entry = iterator.next();

                url
                        .append(entry.getKey())
                        .append('=')
                        .append(entry.getValue().stream().collect(Collectors.joining(",")));

                if (iterator.hasNext()) {
                    url.append('&');
                }
            }
        }

        return url.toString();
    }

    private String formEncode(String data) {
        try {
            return URLEncoder.encode(data, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            // should not happen, UTF-8 is always supported
            throw new IllegalStateException(ex);
        }
    }

    private RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            restTemplate = createRestTemplate();
        }

        return restTemplate;
    }

    private RestTemplate createRestTemplate() {
        return new RestTemplate();
    }
}
