package ru.saidgadjiev.bibliographya.service.api;

import ru.saidgadjiev.bibliographya.domain.AuthenticationKey;
import ru.saidgadjiev.bibliographya.domain.VerificationResult;
import ru.saidgadjiev.bibliographya.domain.SendVerificationResult;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * Created by said on 23/04/2019.
 */
public interface VerificationService {
    
    SendVerificationResult sendVerification(HttpServletRequest request, Locale locale, AuthenticationKey authenticationKey) throws MessagingException;


    VerificationResult verify(HttpServletRequest request, AuthenticationKey authenticationKey, int code, boolean confirm);
}
