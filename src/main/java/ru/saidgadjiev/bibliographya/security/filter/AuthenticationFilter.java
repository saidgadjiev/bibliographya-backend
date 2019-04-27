package ru.saidgadjiev.bibliographya.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import ru.saidgadjiev.bibliographya.data.AuthKeyArgumentResolver;
import ru.saidgadjiev.bibliographya.domain.AuthenticationKey;
import ru.saidgadjiev.bibliographya.exception.handler.PhoneOrEmailIsInvalidException;
import ru.saidgadjiev.bibliographya.model.SignInRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by said on 24.03.2018.
 */
public class AuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String ERROR_MESSAGE = "Something went wrong while parsing /login request body";

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    private final ObjectMapper objectMapper;

    public AuthenticationFilter(ObjectMapper objectMapper) {
        super(new AntPathRequestMatcher("/api/auth/signIn", "POST"));
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String requestBody;

        try {
            requestBody = IOUtils.toString(request.getReader());
            ObjectNode objectNode = objectMapper.readValue(requestBody, ObjectNode.class);
            SignInRequest signInRequest = objectMapper.readValue(requestBody, SignInRequest.class);

            if (objectNode.has("verificationKey")) {
                try {
                    AuthenticationKey authenticationKey = AuthKeyArgumentResolver.resolve(objectNode.get("verificationKey").asText());

                    signInRequest.setAuthenticationKey(authenticationKey);
                } catch (PhoneOrEmailIsInvalidException ex) {
                    throw new BadCredentialsException(messages.getMessage(
                            "AbstractUserDetailsAuthenticationProvider.badCredentials",
                            "Bad credentials"));
                }
            } else {
                throw new BadCredentialsException(messages.getMessage(
                        "AbstractUserDetailsAuthenticationProvider.badCredentials",
                        "Bad credentials"));
            }

            UsernamePasswordAuthenticationToken token
                    = new UsernamePasswordAuthenticationToken(signInRequest.getAuthenticationKey(), signInRequest.getPassword());

            return this.getAuthenticationManager().authenticate(token);
        } catch(IOException e) {
            throw new InternalAuthenticationServiceException(ERROR_MESSAGE, e);
        }
    }
}
