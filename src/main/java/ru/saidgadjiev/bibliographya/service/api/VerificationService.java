package ru.saidgadjiev.bibliographya.service.api;

import ru.saidgadjiev.bibliographya.domain.AuthKey;
import ru.saidgadjiev.bibliographya.domain.VerificationResult;
import ru.saidgadjiev.bibliographya.domain.SendVerificationResult;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * Created by said on 23/04/2019.
 */
public interface VerificationService {
    
    SendVerificationResult sendVerification(HttpServletRequest request, Locale locale, AuthKey authKey) throws MessagingException;

    SendVerificationResult resendVerification(HttpServletRequest request, Locale locale) throws MessagingException;

    VerificationResult verify(HttpServletRequest request, int code, boolean confirm);
}
