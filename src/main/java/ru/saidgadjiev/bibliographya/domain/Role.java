package ru.saidgadjiev.bibliographya.domain;

import org.springframework.security.core.GrantedAuthority;

/**
 * Created by said on 22.10.2018.
 */
public class Role implements GrantedAuthority {

    public static final String ROLE_USER = "ROLE_USER";

    public static final String ROLE_DEVELOPER = "ROLE_DEVELOPER";

    public static final String ROLE_MODERATOR = "ROLE_MODERATOR";

    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    public static final String NAME = "name";

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

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Role role = (Role) object;

        return name != null ? name.equals(role.name) : role.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Role{" +
                "name='" + name + '\'' +
                '}';
    }
}
