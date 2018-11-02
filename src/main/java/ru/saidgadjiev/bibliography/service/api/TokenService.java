package ru.saidgadjiev.bibliography.service.api;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

/**
 * Created by said on 24.10.2018.
 */
public interface TokenService {

    String generate(Map<String, Object> payload);

    Map<String, Object> validate(String token);
}
