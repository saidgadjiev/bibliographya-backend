package ru.saidgadjiev.bibliography.social.oauth;

/**
 * Created by said on 29.12.2018.
 */
public enum ResponseType {

    /**
     * AUTHORIZATION_CODE denotes the server-side authorization flow, and is associated
     * with the response_type=code parameter value
     */
    AUTHORIZATION_CODE,

    /**
     * IMPLICIT_GRANT denotes the client-side authorization flow and is associated with
     * the response_type=token parameter value
     */
    IMPLICIT_GRANT
}
