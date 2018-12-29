package ru.saidgadjiev.bibliography.social.vk;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import ru.saidgadjiev.bibliography.social.facebook.GraphApi;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by said on 29.12.2018.
 */
public class UserProfileOperations {

    private VKApi vkApi;

    UserProfileOperations(VKApi vkApi) {
        this.vkApi = vkApi;
    }

    public ResponseEntity<ObjectNode> getFiels(String userId, String ...fields) {
        return vkApi.get("users.get", new LinkedMultiValueMap<String, String>() {{
            addAll("user_ids", Collections.singletonList(userId));
            addAll("fields", Arrays.asList(fields));
        }});
    }
}
