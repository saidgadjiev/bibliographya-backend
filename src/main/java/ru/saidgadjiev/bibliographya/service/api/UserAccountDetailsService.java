package ru.saidgadjiev.bibliographya.service.api;

import org.springframework.security.core.userdetails.UserDetails;
import ru.saidgadjiev.bibliographya.model.SignUpRequest;

import java.sql.SQLException;

/**
 * Created by said on 22.10.2018.
 */
public interface UserAccountDetailsService {

    UserDetails save(SignUpRequest signUpRequest) throws SQLException;

    UserDetails loadUserById(int userId);

    boolean isExistUserName(String username);
}
