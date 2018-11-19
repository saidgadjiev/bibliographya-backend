package ru.saidgadjiev.bibliography.interceptor;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by said on 29.10.2018.
 */
public class CrossInterceptor extends HandlerInterceptorAdapter {

    public static final String CREDENTIALS_NAME = "Access-Control-Allow-Credentials";
    public static final String ORIGIN_NAME = "Access-Control-Allow-Origin";
    public static final String METHODS_NAME = "Access-Control-Allow-Methods";
    public static final String HEADERS_NAME = "Access-Control-Allow-Headers";
    public static final String MAX_AGE_NAME = "Access-Control-Max-Age";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        response.setHeader(CREDENTIALS_NAME, "true");
        response.setHeader(ORIGIN_NAME, "http://localhost:8080");
        response.setHeader(METHODS_NAME, "PATCH, GET, OPTIONS, POST, PUT, DELETE, HEAD");
        response.setHeader(HEADERS_NAME, "Origin, X-Requested-With, Content-Type, Accept");
        response.setHeader(MAX_AGE_NAME, "3600");

        return true;
    }
}
