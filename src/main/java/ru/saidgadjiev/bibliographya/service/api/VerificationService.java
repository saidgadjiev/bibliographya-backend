package ru.saidgadjiev.bibliographya.service.api;

import ru.saidgadjiev.bibliographya.domain.VerificationResult;
import ru.saidgadjiev.bibliographya.domain.SentVerification;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * Created by said on 23/04/2019.
 */
public interface VerificationService {
    
    SentVerification sendVerification(HttpServletRequest request, Locale locale, String verificationKey) throws MessagingException;


    VerificationResult verify(HttpServletRequest request, String verificationKey, int code);
}
