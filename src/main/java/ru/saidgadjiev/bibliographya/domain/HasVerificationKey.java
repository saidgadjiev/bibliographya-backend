package ru.saidgadjiev.bibliographya.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface HasVerificationKey {

    @JsonIgnore
    AuthenticationKey getAuthenticationKey();

    @JsonIgnore
    void setAuthenticationKey(AuthenticationKey authenticationKey);

}
