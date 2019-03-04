package ru.saidgadjiev.bibliographya.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
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

    private static final Logger LOGGER = Logger.getLogger(HttpAuthenticationEntryPoint.class);

    private final SessionManager sessionManager;

    private final ObjectMapper objectMapper;

    public HttpAuthenticationEntryPoint(SessionManager sessionManager, ObjectMapper objectMapper) {
        this.sessionManager = sessionManager;
        this.objectMapper = objectMapper;
    }

    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        LOGGER.error(authException.getMessage(), authException);

        if (authException instanceof BadCredentialsException) {
            ResponseUtils.sendResponseMessage(response, HttpServletResponse.SC_BAD_REQUEST);
        } else if (authException instanceof DisabledException) {
            ObjectNode objectNode = objectMapper.createObjectNode();

            objectNode.put("email", sessionManager.getEmail(request));

            ResponseUtils.sendResponseMessage(
                    response,
                    HttpStatus.PRECONDITION_REQUIRED.value(),
                    objectMapper.writeValueAsString(objectNode)
            );
        }
    }
}
