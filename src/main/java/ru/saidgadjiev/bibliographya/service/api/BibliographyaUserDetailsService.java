package ru.saidgadjiev.bibliographya.service.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.auth.social.SocialUserInfo;
import ru.saidgadjiev.bibliographya.domain.*;
import ru.saidgadjiev.bibliographya.model.RestorePassword;
import ru.saidgadjiev.bibliographya.model.SavePassword;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by said on 22.10.2018.
 */
public interface BibliographyaUserDetailsService {

    User loadSocialUserById(int userId);

    User loadUserBySocialAccount(ProviderType providerType, String accountId);

    @Transactional
    User saveSocialUser(SocialUserInfo userInfo) throws SQLException;

    User save(User user) throws SQLException;

    User loadUserByUsername(AuthKey authKey) throws UsernameNotFoundException;

    User loadUserById(int id);

    boolean isExist(AuthKey authKey);

    HttpStatus savePassword(SavePassword savePassword);

    SendVerificationResult restorePasswordStart(HttpServletRequest request,
                                    Locale locale,
                                    AuthKey authKey) throws MessagingException;

    HttpStatus restorePasswordFinish(HttpServletRequest request, RestorePassword restorePassword);

    HttpStatus saveEmailFinish(HttpServletRequest request, AuthenticationKeyConfirmation authenticationKeyConfirmation);

    SendVerificationResult saveEmailStart(HttpServletRequest request,
                              Locale locale,
                              AuthKey authKey) throws MessagingException;

    SendVerificationResult savePhoneStart(HttpServletRequest request,
                              Locale locale,
                              AuthKey authKey) throws MessagingException;

    HttpStatus savePhoneFinish(HttpServletRequest request,
                               AuthenticationKeyConfirmation authenticationKeyConfirmation);

    UserProfile getProfile(TimeZone timeZone, int userId);
}
