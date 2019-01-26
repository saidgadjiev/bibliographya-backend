package ru.saidgadjiev.bibliographya.service.api;

import org.springframework.security.core.userdetails.UserDetails;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.auth.social.SocialUserInfo;

import java.sql.SQLException;

/**
 * Created by said on 24.12.2018.
 */
public interface SocialUserDetailsService {

    UserDetails loadSocialUserById(int userId);

    UserDetails loadSocialUserByAccountId(ProviderType providerType, String accountId);

    UserDetails saveSocialUser(SocialUserInfo userInfo) throws SQLException;
}
