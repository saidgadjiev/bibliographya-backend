package ru.saidgadjiev.bibliographya.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.properties.JwtProperties;
import ru.saidgadjiev.bibliographya.properties.UIProperties;
import ru.saidgadjiev.bibliographya.service.impl.AuthTokenService;
import ru.saidgadjiev.bibliographya.utils.CookieUtils;
import ru.saidgadjiev.bibliographya.utils.ResponseUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by said on 25.03.2018.
 */
public class AuthenticationSuccessHandlerImpl extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationSuccessHandlerImpl.class);

    private ObjectMapper objectMapper;

    private AuthTokenService tokenService;

    private UIProperties uiProperties;

    private final JwtProperties jwtProperties;

    private ApplicationEventPublisher eventPublisher;

    public AuthenticationSuccessHandlerImpl(ObjectMapper objectMapper,
                                            AuthTokenService tokenService,
                                            UIProperties uiProperties,
                                            JwtProperties jwtProperties,
                                            ApplicationEventPublisher eventPublisher) {
        this.objectMapper = objectMapper;
        this.tokenService = tokenService;
        this.uiProperties = uiProperties;
        this.jwtProperties = jwtProperties;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        LOGGER.debug("Success signIn");
        User user = (User) authentication.getPrincipal();

        String token = tokenService.createToken(user);

        CookieUtils.addCookie(response, uiProperties.getHost(), jwtProperties.tokenName(), token);
        response.addHeader(
                jwtProperties.tokenName(),
                token
        );

        String body = objectMapper.writeValueAsString(user);

        eventPublisher.publishEvent(new AuthenticationSuccessEvent(authentication));

        ResponseUtils.sendResponseMessage(response, 200, body);
    }
}
