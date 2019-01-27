package ru.saidgadjiev.bibliographya.service.api;

import org.springframework.security.core.userdetails.UserDetails;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.auth.social.SocialUserInfo;
import ru.saidgadjiev.bibliographya.model.SignUpRequest;

import java.sql.SQLException;

/**
 * Created by said on 22.10.2018.
 */
public interface BibliographyaUserDetailsService {

    UserDetails save(SignUpRequest signUpRequest) throws SQLException;

    UserDetails loadUserById(int userId);

    boolean isExistUserName(String username);

    UserDetails loadSocialUserById(int userId);

    UserDetails loadSocialUserByAccountId(ProviderType providerType, String accountId);

    UserDetails saveSocialUser(SocialUserInfo userInfo) throws SQLException;
}
