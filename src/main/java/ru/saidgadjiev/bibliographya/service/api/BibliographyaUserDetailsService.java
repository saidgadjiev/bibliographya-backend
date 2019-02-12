package ru.saidgadjiev.bibliographya.service.api;

import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.auth.social.SocialUserInfo;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.model.SignUpRequest;

import java.sql.SQLException;

/**
 * Created by said on 22.10.2018.
 */
public interface BibliographyaUserDetailsService {

    User save(SignUpRequest signUpRequest) throws SQLException;

    User loadUserById(int userId);

    boolean isExistUserName(String username);

    User loadSocialUserById(int userId);

    User loadSocialUserByAccountId(ProviderType providerType, String accountId);

    User saveSocialUser(SocialUserInfo userInfo) throws SQLException;
}
