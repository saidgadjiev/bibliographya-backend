package ru.saidgadjiev.bibliographya.service.api;

import java.util.Map;

/**
 * Created by said on 24.10.2018.
 */
public interface TokenService {

    String generate(Map<String, Object> payload);

    Map<String, Object> validate(String token);
}
