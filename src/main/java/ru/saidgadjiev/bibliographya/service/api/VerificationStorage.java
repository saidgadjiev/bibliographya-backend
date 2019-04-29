package ru.saidgadjiev.bibliographya.service.api;

import javax.servlet.http.HttpServletRequest;

public interface VerificationStorage {

    String STATE = "state";

    String FIRST_NAME = "firstName";

    String SIGN_UP_REQUEST = "signUpRequest";

    String AUTH_KEY = "authKey";

    String TIMER = "timer";

    void removeAttr(HttpServletRequest request, String attr);

    Object getAttr(HttpServletRequest request, String attr);

    Object getAttr(HttpServletRequest request, String attr, Object defaultValue);

    void setAttr(HttpServletRequest request, String attr, Object data);
    
    default void expire(HttpServletRequest request) {
        
    }
}
