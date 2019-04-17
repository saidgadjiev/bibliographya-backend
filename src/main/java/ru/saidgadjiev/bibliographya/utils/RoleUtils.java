package ru.saidgadjiev.bibliographya.utils;

import ru.saidgadjiev.bibliographya.domain.Role;

import java.util.Set;

public class RoleUtils {

    private RoleUtils() { }

    public static boolean hasAnyRole(Set<Role> roleSet, String... roles) {
        for (String role : roles) {
            if (roleSet.contains(new Role(role))) {
                return true;
            }
        }

        return false;
    }
}
