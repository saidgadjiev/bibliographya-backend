package ru.saidgadjiev.bibliographya.http.message;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import ru.saidgadjiev.bibliographya.data.AuthKeyArgumentResolver;
import ru.saidgadjiev.bibliographya.domain.HasAuthKey;
import ru.saidgadjiev.bibliographya.domain.AuthenticationKey;

import java.io.IOException;
import java.lang.reflect.Type;

public class AuthKeyMessageConverter extends MappingJackson2HttpMessageConverter {

    private ObjectMapper objectMapper;

    public AuthKeyMessageConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return HasAuthKey.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
        return HasAuthKey.class.isAssignableFrom(contextClass);
    }

    @Override
    public HasAuthKey read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        ObjectNode objectNode = objectMapper.readValue(inputMessage.getBody(), ObjectNode.class);
        JavaType javaType = getJavaType(type, contextClass);

        HasAuthKey hasAuthKey = objectMapper.readValue(inputMessage.getBody(), javaType);

        if (objectNode.has("verificationKey")) {
            AuthenticationKey authenticationKey = AuthKeyArgumentResolver.resolve(objectNode.get("verificationKey").asText());

            hasAuthKey.setAuthenticationKey(authenticationKey);
        }

        return hasAuthKey;
    }
}
