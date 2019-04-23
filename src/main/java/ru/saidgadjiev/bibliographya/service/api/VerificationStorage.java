package ru.saidgadjiev.bibliographya.service.api;

import javax.servlet.http.HttpServletRequest;

public interface VerificationStorage {

    public static final String STATE = "state";

    public static final String FIRST_NAME = "firstName";

    public static final String LAST_NAME = "lastName";

    public static final String MIDDLE_NAME = "middleName";

    public static final String SIGN_UP_REQUEST = "signUpRequest";

    public static final String CODE = "code";

    public static final String VERIFICATION_KEY = "verificationKey";

    void removeAttr(HttpServletRequest request, String attr);

    Object getAttr(HttpServletRequest request, String attr);

    void setAttr(HttpServletRequest request, String attr, Object data);
    
    default void expire(HttpServletRequest request) {
        
    }
}
