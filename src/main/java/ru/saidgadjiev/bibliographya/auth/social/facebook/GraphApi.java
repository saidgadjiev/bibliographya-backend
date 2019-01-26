package ru.saidgadjiev.bibliographya.auth.social.facebook;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

/**
 * Created by said on 29.12.2018.
 */
public interface GraphApi {

    ResponseEntity<ObjectNode> get(String graphId, MultiValueMap<String, String> parameters);

    void delete(String graphId, MultiValueMap<String, String> parameters);
}
