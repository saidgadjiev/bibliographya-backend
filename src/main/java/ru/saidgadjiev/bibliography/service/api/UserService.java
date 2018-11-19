package ru.saidgadjiev.bibliography.service.api;

import ru.saidgadjiev.bibliography.model.SignUpRequest;

/**
 * Created by said on 22.10.2018.
 */
public interface UserService {

    void save(SignUpRequest signUpRequest);

    boolean isExistUserName(String username);
}
