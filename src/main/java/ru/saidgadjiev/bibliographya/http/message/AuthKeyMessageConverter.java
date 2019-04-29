package ru.saidgadjiev.bibliographya.http.message;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import ru.saidgadjiev.bibliographya.data.AuthKeyArgumentResolver;
import ru.saidgadjiev.bibliographya.domain.HasAuthKey;
import ru.saidgadjiev.bibliographya.domain.AuthKey;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

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
        return HasAuthKey.class.isAssignableFrom((Class<?>) type);
    }

    @Override
    public HasAuthKey read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        String body = IOUtils.toString(inputMessage.getBody(), Charset.defaultCharset());
        ObjectNode objectNode = objectMapper.readValue(body, ObjectNode.class);
        JavaType javaType = getJavaType(type, contextClass);

        HasAuthKey hasAuthKey = objectMapper.readValue(body, javaType);

        if (objectNode.has("verificationKey")) {
            AuthKey authKey = AuthKeyArgumentResolver.resolve(objectNode.get("verificationKey").asText());

            hasAuthKey.setAuthKey(authKey);
        }

        return hasAuthKey;
    }
}
