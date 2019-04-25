package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.domain.SentVerification;
import ru.saidgadjiev.bibliographya.domain.VerificationResult;
import ru.saidgadjiev.bibliographya.service.api.VerificationService;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * Created by said on 23/04/2019.
 */
@Service
@Qualifier("phone")
public class PhoneVerificationService implements VerificationService {
    @Override
    public SentVerification sendVerification(HttpServletRequest request, Locale locale, String verificationKey) throws MessagingException {
        return null;
    }

    @Override
    public VerificationResult verify(HttpServletRequest request, String verificationKey, int code) {
        return null;
    }
}
