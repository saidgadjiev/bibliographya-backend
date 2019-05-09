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
import org.springframework.util.AntPathMatcher;
import org.springframework.web.client.HttpClientErrorException;
import ru.saidgadjiev.bibliographya.auth.common.AuthContext;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.data.AuthKeyArgumentResolver;
import ru.saidgadjiev.bibliographya.domain.AuthKey;
import ru.saidgadjiev.bibliographya.exception.handler.PhoneOrEmailIsInvalidException;
import ru.saidgadjiev.bibliographya.model.SignInRequest;
import ru.saidgadjiev.bibliographya.security.token.SocialUserAuthenticationToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Created by said on 24.03.2018.
 */
public class AuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String ERROR_MESSAGE = "Something went wrong while parsing /login request body";

    private static final String PATTERN = "/api/auth/signIn/{providerId}";

    private MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    private final ObjectMapper objectMapper;

    public AuthenticationFilter(ObjectMapper objectMapper) {
        super(new AntPathRequestMatcher(PATTERN, "POST"));
        this.objectMapper = objectMapper;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            AntPathMatcher apm = new AntPathMatcher();
            Map<String, String> pathVariables = apm.extractUriTemplateVariables(PATTERN, request.getRequestURI());

            String providerId = pathVariables.get("providerId");
            ProviderType providerType = ProviderType.fromId(providerId);

            if (providerType == null) {
                throw new InternalAuthenticationServiceException("Provider not found");
            }

            switch (providerType) {
                case VK: {
                    AuthContext authContext = new AuthContext()
                            .setProviderType(providerType)
                            .setCode(request.getParameter("code"))
                            .setRedirectUri(request.getParameter("redirectUri"));

                    SocialUserAuthenticationToken token
                            = new SocialUserAuthenticationToken(null, null);

                    token.setDetails(authContext);

                    return this.getAuthenticationManager().authenticate(token);
                }
                case SIMPLE: {
                    String requestBody = IOUtils.toString(request.getReader());
                    ObjectNode objectNode = objectMapper.readValue(requestBody, ObjectNode.class);
                    SignInRequest signInRequest = objectMapper.readValue(requestBody, SignInRequest.class);

                    if (objectNode.has("verificationKey")) {
                        try {
                            AuthKey authKey = AuthKeyArgumentResolver.resolve(objectNode.get("verificationKey").asText());

                            signInRequest.setAuthKey(authKey);
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
                            = new UsernamePasswordAuthenticationToken(signInRequest.getAuthKey(), signInRequest.getPassword());

                    return this.getAuthenticationManager().authenticate(token);
                }
                default:
                    throw new InternalAuthenticationServiceException("Facebook provider not supported yet");
            }
        } catch (IOException e) {
            throw new InternalAuthenticationServiceException(ERROR_MESSAGE, e);
        }
    }
}
