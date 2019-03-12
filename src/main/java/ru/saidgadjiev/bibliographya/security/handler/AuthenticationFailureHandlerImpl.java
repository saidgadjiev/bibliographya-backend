package ru.saidgadjiev.bibliographya.security.handler;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import ru.saidgadjiev.bibliographya.utils.ResponseUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthenticationFailureHandlerImpl extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException authException) throws IOException {
        if (authException instanceof BadCredentialsException || authException instanceof UsernameNotFoundException) {
            ResponseUtils.sendResponseMessage(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED
            );
        }
    }
}
