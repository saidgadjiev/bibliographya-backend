package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.domain.AuthKey;
import ru.saidgadjiev.bibliographya.domain.SendVerificationResult;
import ru.saidgadjiev.bibliographya.domain.VerificationResult;
import ru.saidgadjiev.bibliographya.service.api.BruteForceService;
import ru.saidgadjiev.bibliographya.service.api.VerificationService;
import ru.saidgadjiev.bibliographya.service.api.VerificationStorage;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * Created by said on 24.02.2019.
 */
@Service
@Qualifier("wrapper")
public class VerificationServiceWrapper implements VerificationService {

    private VerificationStorage verificationStorage;

    private VerificationService emailVerificationService;

    private VerificationService phoneVerificationService;

    private ru.saidgadjiev.bibliographya.service.api.BruteForceService bruteForceService;

    @Autowired
    public VerificationServiceWrapper(@Qualifier("inMemory") VerificationStorage verificationStorage,
                                      @Qualifier("email") VerificationService emailVerificationService,
                                      @Qualifier("phone") VerificationService phoneVerificationService,
                                      BruteForceService bruteForceService) {
        this.verificationStorage = verificationStorage;
        this.emailVerificationService = emailVerificationService;
        this.phoneVerificationService = phoneVerificationService;
        this.bruteForceService = bruteForceService;
    }

    @Override
    public SendVerificationResult sendVerification(HttpServletRequest request,
                                                   Locale locale,
                                                   AuthKey authKey) throws MessagingException {
        if (bruteForceService.isBlocked(request, ru.saidgadjiev.bibliographya.service.api.BruteForceService.Type.SEND_VERIFICATION_CODE)) {
            return new SendVerificationResult(HttpStatus.TOO_MANY_REQUESTS, null, null);
        }

        bruteForceService.count(request, ru.saidgadjiev.bibliographya.service.api.BruteForceService.Type.SEND_VERIFICATION_CODE);

        switch (authKey.getType()) {
            case PHONE:
                return phoneVerificationService.sendVerification(request, locale, authKey);
            case EMAIL:
                return emailVerificationService.sendVerification(request, locale, authKey);
        }

        return null;
    }

    @Override
    public SendVerificationResult resendVerification(HttpServletRequest request, Locale locale) throws MessagingException {
        if (bruteForceService.isBlocked(request, ru.saidgadjiev.bibliographya.service.api.BruteForceService.Type.SEND_VERIFICATION_CODE)) {
            return new SendVerificationResult(HttpStatus.TOO_MANY_REQUESTS, null, null);
        }

        bruteForceService.count(request, ru.saidgadjiev.bibliographya.service.api.BruteForceService.Type.SEND_VERIFICATION_CODE);

        AuthKey authKey = (AuthKey) verificationStorage.getAttr(request, VerificationStorage.AUTH_KEY, null);

        if (authKey == null) {
            return new SendVerificationResult(HttpStatus.BAD_REQUEST, null, null);
        }

        switch (authKey.getType()) {
            case PHONE:
                return phoneVerificationService.resendVerification(request, locale);
            case EMAIL:
                return emailVerificationService.resendVerification(request, locale);
        }

        return new SendVerificationResult(HttpStatus.BAD_REQUEST, null, null);
    }

    @Override
    public VerificationResult verify(HttpServletRequest request, int code, boolean confirm) {
        AuthKey authKey = (AuthKey) verificationStorage.getAttr(request, VerificationStorage.AUTH_KEY, null);

        if (authKey == null) {
            return new VerificationResult().setStatus(VerificationResult.Status.INVALID);
        }

        switch (authKey.getType()) {
            case PHONE:
                return phoneVerificationService.verify(request, code, confirm);
            case EMAIL:
                return emailVerificationService.verify(request, code, confirm);
        }

        return null;
    }
}
