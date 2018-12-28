package ru.saidgadjiev.bibliography.service.impl.auth.social;

import org.springframework.security.core.userdetails.UserDetails;
import ru.saidgadjiev.bibliography.auth.ProviderType;
import ru.saidgadjiev.bibliography.auth.SocialUserInfo;

import java.sql.SQLException;

/**
 * Created by said on 24.12.2018.
 */
public interface SocialUserDetailsService {

    UserDetails loadSocialUserById(int userId);

    UserDetails loadSocialUserByAccountId(ProviderType providerType, String accountId);

    UserDetails saveSocialUser(SocialUserInfo userInfo) throws SQLException;
}
