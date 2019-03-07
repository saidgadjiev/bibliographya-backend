package ru.saidgadjiev.bibliographya.service.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
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
public interface BibliographyaUserDetailsService extends UserDetailsService {

    User save(SignUpRequest signUpRequest) throws SQLException;

    User loadUserAccountById(int id);

    boolean isExistEmail(String username);

    User loadSocialUserById(int userId);

    User loadSocialUserByAccountId(ProviderType providerType, String accountId);

    User saveSocialUser(SocialUserInfo userInfo) throws SQLException;

    HttpStatus savePassword(SavePassword savePassword);

    HttpStatus restorePasswordStart(HttpServletRequest request, Locale locale, String email);

    HttpStatus restorePasswordFinish(HttpServletRequest request, RestorePassword restorePassword);

    HttpStatus saveEmailFinish(HttpServletRequest request, SaveEmail saveEmail);

    HttpStatus saveEmailStart(HttpServletRequest request, Locale locale, String email);
}
