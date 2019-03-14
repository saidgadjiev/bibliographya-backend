package ru.saidgadjiev.bibliographya.auth.social;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OAuthTemplate {

    private RestTemplate restTemplate;

    private String clientId;

    private String clientSecret;

    private final String oauthUrl;

    private final String accessTokenUrl;

    public OAuthTemplate(String clientId, String clientSecret, String oauthUrl, String accessTokenUrl) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.oauthUrl = oauthUrl;
        this.accessTokenUrl = accessTokenUrl;
    }

    public String buildOAuthUrl(String redirectUri, MultiValueMap<String, String> parameters) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        if (parameters != null) {
            params.addAll(parameters);
        }

        params.set("response_type", ResponseType.AUTHORIZATION_CODE.getDesc());
        params.set("client_id", formEncode(clientId));
        params.set("redirect_uri", redirectUri);

        return buildUrl(oauthUrl, params);
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

        String url = buildUrl(accessTokenUrl, params);

        ResponseEntity<ObjectNode> exchangeResponse = getRestTemplate().getForEntity(url, ObjectNode.class);
        ObjectNode body = exchangeResponse.getBody();

        return new AccessGrant(
                body.get("access_token").asText(),
                body.get("expires_in").asLong(),
                body.get("user_id").asText()
        );
    }

    private String formEncode(String data) {
        try {
            return URLEncoder.encode(data, StandardCharsets.UTF_8.name());
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
