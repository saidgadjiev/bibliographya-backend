package ru.saidgadjiev.bibliographya.auth.social.vk;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

/**
 * Created by said on 29.12.2018.
 */
public interface VKApi {

    ResponseEntity<ObjectNode> get(String method, MultiValueMap<String, String> parameters);


}
