package ru.saidgadjiev.bibliographya.auth.social;

/**
 * Created by said on 29.12.2018.
 */
public enum ResponseType {

    /**
     * AUTHORIZATION_CODE denotes the server-side authorization flow, and is associated
     * with the response_type=code parameter value
     */
    AUTHORIZATION_CODE("code"),

    /**
     * IMPLICIT_GRANT denotes the client-side authorization flow and is associated with
     * the response_type=token parameter value
     */
    IMPLICIT_GRANT("token");

    private final String desc;

    ResponseType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public static ResponseType fromDesc(String desc) {
        for (ResponseType value: values()) {
            if (value.desc.equals(desc)) {
                return value;
            }
        }

        return null;
    }
}
