package ru.saidgadjiev.bibliographya.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import ru.saidgadjiev.bibliographya.service.impl.SessionManager;
import ru.saidgadjiev.bibliographya.utils.ResponseUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by said on 25.03.2018.
 */
public class HttpAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpAuthenticationEntryPoint.class);

    private final SessionManager sessionManager;

    private final ObjectMapper objectMapper;

    public HttpAuthenticationEntryPoint(SessionManager sessionManager, ObjectMapper objectMapper) {
        this.sessionManager = sessionManager;
        this.objectMapper = objectMapper;
    }

    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        LOGGER.error(authException.getMessage(), authException);

        if (authException instanceof BadCredentialsException || authException instanceof UsernameNotFoundException) {
            ResponseUtils.sendResponseMessage(response, HttpServletResponse.SC_UNAUTHORIZED);
        } else if (authException instanceof DisabledException) {
            ObjectNode objectNode = objectMapper.createObjectNode();

            objectNode.put("state", sessionManager.getState(request).name());
            objectNode.put("email", sessionManager.getEmail(request));

            ResponseUtils.sendResponseMessage(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    objectMapper.writeValueAsString(objectNode)
            );
        }
    }
}
