package ru.saidgadjiev.bibliographya.utils;

import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.domain.*;

import java.util.Set;

public class TestModelsUtils {

    private TestModelsUtils() {
    }

    public static User createUser(int id,
                                  String firstName,
                                  String lastName,
                                  String middleName,
                                  ProviderType providerType,
                                  Set<Role> roles,
                                  UserAccount userAccount,
                                  SocialAccount socialAccount) {
        User user = new User();

        user.setId(1);
        user.setRoles(roles);
        user.setUserAccount(userAccount);
        user.setSocialAccount(socialAccount);
        user.setProviderType(providerType);

        Biography biography = new Biography();

        biography.setId(id);
        biography.setFirstName(firstName);
        biography.setLastName(lastName);
        biography.setMiddleName(middleName);
        biography.setUserId(id);
        biography.setCreatorId(id);

        user.setBiography(biography);

        return user;
    }
}
