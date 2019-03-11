package ru.saidgadjiev.bibliographya.auth.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by said on 27.12.2018.
 */
public class AuthContext {

    private HttpServletRequest request;

    private HttpServletResponse response;

    private ProviderType providerType;

    private String code;

    private Object body;

    public HttpServletRequest getRequest() {
        return request;
    }

    public AuthContext setRequest(HttpServletRequest request) {
        this.request = request;

        return this;
    }

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

    public Object getBody() {
        return body;
    }

    public AuthContext setBody(Object body) {
        this.body = body;

        return this;
    }
}
