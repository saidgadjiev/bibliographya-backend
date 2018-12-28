package ru.saidgadjiev.bibliography.service.impl.auth;

import ru.saidgadjiev.bibliography.auth.ProviderType;
import ru.saidgadjiev.bibliography.model.SignInRequest;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by said on 27.12.2018.
 */
public class AuthContext {

    private HttpServletResponse response;

    private ProviderType providerType;

    private String code;

    private SignInRequest signInRequest;

    public HttpServletResponse getResponse() {
        return response;
    }

    public AuthContext setResponse(HttpServletResponse response) {
        this.response = response;

        return this;
    }

    public ProviderType getProviderType() {
        return providerType;
    }

    public AuthContext setProviderType(ProviderType providerType) {
        this.providerType = providerType;

        return this;
    }

    public String getCode() {
        return code;
    }

    public AuthContext setCode(String code) {
        this.code = code;

        return this;
    }

    public SignInRequest getSignInRequest() {
        return signInRequest;
    }

    public AuthContext setSignInRequest(SignInRequest signInRequest) {
        this.signInRequest = signInRequest;

        return this;
    }
}
