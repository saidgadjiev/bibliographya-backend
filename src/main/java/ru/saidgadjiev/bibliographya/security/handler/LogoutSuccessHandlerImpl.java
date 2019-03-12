package ru.saidgadjiev.bibliographya.security.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import ru.saidgadjiev.bibliographya.security.event.SignOutSuccessEvent;
import ru.saidgadjiev.bibliographya.utils.ResponseUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogoutSuccessHandlerImpl.class);

    private ApplicationEventPublisher eventPublisher;

    public LogoutSuccessHandlerImpl(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        LOGGER.debug("Success signOut");
        if (authentication == null) {
            ResponseUtils.sendResponseMessage(httpServletResponse, HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            ResponseUtils.sendResponseMessage(httpServletResponse, HttpServletResponse.SC_OK);

            eventPublisher.publishEvent(new SignOutSuccessEvent(authentication));
        }
    }
}
