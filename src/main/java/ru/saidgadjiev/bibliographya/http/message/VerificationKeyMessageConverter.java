package ru.saidgadjiev.bibliographya.http.message;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import ru.saidgadjiev.bibliographya.data.VerificationKeyArgumentResolver;
import ru.saidgadjiev.bibliographya.domain.HasVerificationKey;
import ru.saidgadjiev.bibliographya.domain.AuthenticationKey;

import java.io.IOException;
import java.lang.reflect.Type;

public class VerificationKeyMessageConverter extends MappingJackson2HttpMessageConverter {

    private ObjectMapper objectMapper;

    public VerificationKeyMessageConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return HasVerificationKey.class.isAssignableFrom(clazz);
    }

    @Override
    public HasVerificationKey read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        ObjectNode objectNode = objectMapper.readValue(inputMessage.getBody(), ObjectNode.class);
        JavaType javaType = getJavaType(type, contextClass);

        HasVerificationKey hasVerificationKey = objectMapper.readValue(inputMessage.getBody(), javaType);

        if (objectNode.has("verificationKey")) {
            AuthenticationKey authenticationKey = VerificationKeyArgumentResolver.resolve(objectNode.get("verificationKey").asText());

            hasVerificationKey.setAuthenticationKey(authenticationKey);
        }

        return hasVerificationKey;
    }
}
