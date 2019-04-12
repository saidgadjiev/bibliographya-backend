package ru.saidgadjiev.bibliographya.service.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import ru.saidgadjiev.bibliographya.domain.EmailConfirmation;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.domain.UserAccount;
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
public interface BibliographyaUserDetailsService extends UserDetailsService {

    User save(User user) throws SQLException;

    User loadUserById(int id);

    boolean isExistEmail(String username);

    HttpStatus savePassword(SavePassword savePassword);

    HttpStatus restorePasswordStart(HttpServletRequest request, Locale locale, String email) throws MessagingException;

    HttpStatus restorePasswordFinish(HttpServletRequest request, RestorePassword restorePassword);

    HttpStatus saveEmailFinish(HttpServletRequest request, EmailConfirmation emailConfirmation);

    HttpStatus saveEmailStart(HttpServletRequest request, Locale locale, String email) throws MessagingException;

    UserAccount getAccount(TimeZone timeZone, int userId);
}
