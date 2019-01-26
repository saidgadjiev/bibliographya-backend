package ru.saidgadjiev.bibliographya.security.handler;

import org.apache.log4j.Logger;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import ru.saidgadjiev.bibliographya.utils.ResponseUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by said on 25.03.2018.
 */
public class Http403AccessDeniedEntryPoint implements AccessDeniedHandler {

    private static final Logger LOGGER = Logger.getLogger(Http403AccessDeniedEntryPoint.class);

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        LOGGER.debug("Access denied");
        LOGGER.error(accessDeniedException.getMessage(), accessDeniedException);

        ResponseUtils.sendResponseMessage(response, HttpServletResponse.SC_FORBIDDEN);
    }
}
