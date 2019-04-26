package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.domain.SendVerificationResult;
import ru.saidgadjiev.bibliographya.domain.AuthenticationKey;
import ru.saidgadjiev.bibliographya.domain.VerificationResult;
import ru.saidgadjiev.bibliographya.service.api.VerificationService;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * Created by said on 24.02.2019.
 */
@Service
@Qualifier("wrapper")
public class VerificationServiceWrapper implements VerificationService {

    private VerificationService emailVerificationService;

    private VerificationService phoneVerificationService;

    @Autowired
    public VerificationServiceWrapper(@Qualifier("email") VerificationService emailVerificationService,
                                      @Qualifier("phone") VerificationService phoneVerificationService) {
        this.emailVerificationService = emailVerificationService;
        this.phoneVerificationService = phoneVerificationService;
    }

    @Override
    public SendVerificationResult sendVerification(HttpServletRequest request,
                                                   Locale locale,
                                                   AuthenticationKey authenticationKey) throws MessagingException {
        switch (authenticationKey.getType()) {
            case PHONE:
                return phoneVerificationService.sendVerification(request, locale, authenticationKey);
            case EMAIL:
                return emailVerificationService.sendVerification(request, locale, authenticationKey);
        }

        return null;
    }

    @Override
    public VerificationResult verify(HttpServletRequest request, AuthenticationKey authenticationKey, int code) {
        switch (authenticationKey.getType()) {
            case PHONE:
                return phoneVerificationService.verify(request, authenticationKey, code);
            case EMAIL:
                return emailVerificationService.verify(request, authenticationKey, code);
        }

        return null;
    }
}
