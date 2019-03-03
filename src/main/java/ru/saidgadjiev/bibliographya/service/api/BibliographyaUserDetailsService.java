package ru.saidgadjiev.bibliographya.service.api;

import org.springframework.http.HttpStatus;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.auth.social.SocialUserInfo;
import ru.saidgadjiev.bibliographya.domain.SaveEmail;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.model.RestorePassword;
import ru.saidgadjiev.bibliographya.model.SavePassword;
import ru.saidgadjiev.bibliographya.model.SignUpRequest;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.Locale;

/**
 * Created by said on 22.10.2018.
 */
public interface BibliographyaUserDetailsService {

    User save(SignUpRequest signUpRequest) throws SQLException;

    User loadUserById(int userId);

    boolean isExistEmail(String username);

    User loadSocialUserById(int userId);

    User loadSocialUserByAccountId(ProviderType providerType, String accountId);

    User saveSocialUser(SocialUserInfo userInfo) throws SQLException;

    HttpStatus savePassword(SavePassword savePassword);

    HttpStatus restorePassword(HttpServletRequest request, Locale locale, String email);

    HttpStatus restorePassword(HttpServletRequest request, RestorePassword restorePassword);

    HttpStatus saveEmail(HttpServletRequest request, SaveEmail saveEmail);

    HttpStatus changeEmail(HttpServletRequest request, Locale locale, String newEmail);
}
