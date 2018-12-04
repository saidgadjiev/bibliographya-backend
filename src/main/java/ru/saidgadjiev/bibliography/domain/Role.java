package ru.saidgadjiev.bibliography.domain;

import org.springframework.security.core.GrantedAuthority;

/**
 * Created by said on 22.10.2018.
 */
public class Role implements GrantedAuthority {

    private final String name;

    public Role(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getAuthority() {
        return name;
    }
}
