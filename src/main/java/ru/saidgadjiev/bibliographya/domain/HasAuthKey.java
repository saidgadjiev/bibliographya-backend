package ru.saidgadjiev.bibliographya.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface HasAuthKey {

    @JsonIgnore
    AuthenticationKey getAuthenticationKey();

    @JsonIgnore
    void setAuthenticationKey(AuthenticationKey authenticationKey);

}
