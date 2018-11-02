package ru.saidgadjiev.bibliography.domain;

import java.util.Set;

/**
 * Created by said on 22.10.2018.
 */
public class User {

    private final String name;

    private final String password;

    private Set<Role> roles;

    public User(String name, String password, Set<Role> roles) {
        this.name = name;
        this.password = password;
        this.roles = roles;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public Set<Role> getRoles() {
        return roles;
    }
}
