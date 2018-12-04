package ru.saidgadjiev.bibliography.service.api;

import ru.saidgadjiev.bibliography.model.SignUpRequest;

import java.sql.SQLException;

/**
 * Created by said on 22.10.2018.
 */
public interface UserService {

    void save(SignUpRequest signUpRequest) throws SQLException;

    boolean isExistUserName(String username);
}
