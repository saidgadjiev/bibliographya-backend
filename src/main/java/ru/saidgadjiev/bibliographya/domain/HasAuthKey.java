package ru.saidgadjiev.bibliographya.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface HasAuthKey {

    @JsonIgnore
    AuthKey getAuthKey();

    @JsonIgnore
    void setAuthKey(AuthKey authKey);

}
