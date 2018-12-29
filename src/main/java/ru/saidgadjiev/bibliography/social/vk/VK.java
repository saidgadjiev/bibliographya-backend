package ru.saidgadjiev.bibliography.social.vk;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by said on 29.12.2018.
 */
public class VK implements VKApi {

    private static final String API_VERSION = "5.92";

    private static final String VK_API_URL = "https://api.vk.com/method/";

    private final RestTemplate restTemplate;

    private UserProfileOperations userProfileOperations;

    private String accessToken;

    public VK(String accessToken) {
        this.accessToken = accessToken;
        this.restTemplate = createRestTemplate();
        initApis();
    }

    @Override
    public ResponseEntity<ObjectNode> get(String method, MultiValueMap<String, String> parameters) {
        return restTemplate.getForEntity(buildMethodUrl(method, parameters), ObjectNode.class);
    }

    public UserProfileOperations getUserProfileOperations() {
        return userProfileOperations;
    }

    private void initApis() {
        userProfileOperations = new UserProfileOperations(this);
    }

    private String buildMethodUrl(String method, MultiValueMap<String, String> parameters) {
        StringBuilder url = new StringBuilder(VK_API_URL);

        url.append(method);
        url.append('?').append("access_token").append('=').append(accessToken);
        url.append('&').append("v").append('=').append(API_VERSION);

        if (parameters != null) {
            for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
                url
                        .append('&')
                        .append(entry.getKey())
                        .append('=')
                        .append(entry.getValue().stream().collect(Collectors.joining(",")));

            }
        }

        return url.toString();
    }

    private RestTemplate createRestTemplate() {
        return new RestTemplate();
    }
}
