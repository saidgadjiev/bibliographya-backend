package ru.saidgadjiev.bibliographya.service.api;

import javax.servlet.http.HttpServletRequest;

public interface VerificationStorage {

    String STATE = "state";

    String FIRST_NAME = "firstName";

    String LAST_NAME = "lastName";

    String MIDDLE_NAME = "middleName";

    String SIGN_UP_REQUEST = "signUpRequest";

    String TIMER = "timer";

    String CODE = "code";

    String VERIFICATION_KEY = "verificationKey";

    void removeAttr(HttpServletRequest request, String attr);

    Object getAttr(HttpServletRequest request, String attr);

    Object getAttr(HttpServletRequest request, String attr, Object defaultValue);

    void setAttr(HttpServletRequest request, String attr, Object data);
    
    default void expire(HttpServletRequest request) {
        
    }
}
