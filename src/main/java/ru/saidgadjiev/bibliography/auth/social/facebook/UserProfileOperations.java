package ru.saidgadjiev.bibliography.auth.social.facebook;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Arrays;

/**
 * Created by said on 29.12.2018.
 */
public class UserProfileOperations {

    private GraphApi graphApi;

    UserProfileOperations(GraphApi graphApi) {
        this.graphApi = graphApi;
    }

    public ResponseEntity<ObjectNode> getFiels(String ...fields) {
        return graphApi.get("me", new LinkedMultiValueMap<String, String>() {{
            addAll("fields", Arrays.asList(fields));
        }});
    }
}
