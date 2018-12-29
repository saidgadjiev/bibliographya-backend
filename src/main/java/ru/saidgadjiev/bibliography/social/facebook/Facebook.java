package ru.saidgadjiev.bibliography.social.facebook;

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
public class Facebook implements GraphApi {

    private static final String API_VERSION = "3.2";

    private static final String GRAPH_API_URL = "https://graph.facebook.com/v" + API_VERSION + "/";

    private final RestTemplate restTemplate;

    private String accessToken;

    private PermissionOperations permissionOperations;

    private UserProfileOperations userProfileOperations;

    public Facebook(String accessToken) {
        this.accessToken = accessToken;
        this.restTemplate = createRestTemplate();

        initApis();
    }

    @Override
    public ResponseEntity<ObjectNode> get(String graphId, MultiValueMap<String, String> parameters) {
        return restTemplate.getForEntity(buildGraphUrl(graphId, parameters), ObjectNode.class);
    }

    @Override
    public void delete(String graphId, MultiValueMap<String, String> parameters) {
        restTemplate.delete(buildGraphUrl(graphId, null));
    }

    public UserProfileOperations getUserProfileOperations() {
        return userProfileOperations;
    }

    public PermissionOperations getPermissionOperations() {
        return permissionOperations;
    }

    private void initApis() {
        userProfileOperations = new UserProfileOperations(this);
        permissionOperations = new PermissionOperations(this);
    }

    private String buildGraphUrl(String graphId, MultiValueMap<String, String> parameters) {
        StringBuilder url = new StringBuilder(GRAPH_API_URL);

        url.append(graphId);
        url.append('?').append("access_token").append('=').append(accessToken);

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
