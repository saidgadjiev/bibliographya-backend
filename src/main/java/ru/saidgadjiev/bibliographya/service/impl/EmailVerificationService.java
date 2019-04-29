package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.dao.api.VerificationDao;
import ru.saidgadjiev.bibliographya.domain.*;
import ru.saidgadjiev.bibliographya.model.SessionState;
import ru.saidgadjiev.bibliographya.model.SignUpRequest;
import ru.saidgadjiev.bibliographya.service.api.EmailService;
import ru.saidgadjiev.bibliographya.service.api.VerificationStorage;
import ru.saidgadjiev.bibliographya.utils.SecureUtils;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Created by said on 25/04/2019.
 */
@Service
@Qualifier("email")
public class EmailVerificationService extends AbstractVerificationService {

    private VerificationStorage coldVerificationStorage;

    private VerificationDao verificationDao;

    private final CodeGenerator codeGenerator;

    private final EmailService emailService;

    private MessageSource messageSource;

    private SecurityService securityService;

    @Autowired
    public EmailVerificationService(@Qualifier("inMemory") VerificationStorage verificationStorage,
                                    VerificationDao verificationDao,
                                    CodeGenerator codeGenerator,
                                    EmailService emailService,
                                    MessageSource messageSource,
                                    SecurityService securityService) {
        super(verificationStorage, verificationDao);
        this.coldVerificationStorage = verificationStorage;
        this.verificationDao = verificationDao;
        this.codeGenerator = codeGenerator;
        this.emailService = emailService;
        this.messageSource = messageSource;
        this.securityService = securityService;
    }

    @Override
    public SendVerificationResult sendVerification(HttpServletRequest request, Locale locale, AuthKey authKey) throws MessagingException {
        SessionState sessionState = (SessionState) coldVerificationStorage.getAttr(request, VerificationStorage.STATE, SessionState.NONE);

        if (!Objects.equals(sessionState, SessionState.NONE)) {
            int code = codeGenerator.generate();
            long expiredAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2);

            Verification verification = new Verification();

            verification.setVerificationKey(authKey.getEmail());
            verification.setCode(String.valueOf(code));
            verification.setExipredAt(expiredAt);

            verificationDao.create(verification);

            emailService.sendEmail(
                    authKey.getEmail(),
                    getEmailSubject(request, locale),
                    getEmailMessage(request, locale, String.valueOf(code))
            );

            coldVerificationStorage.setAttr(request, VerificationStorage.AUTH_KEY, authKey);

            return new SendVerificationResult(HttpStatus.OK, null, SecureUtils.secureEmail(authKey.getEmail()));
        }

        return new SendVerificationResult(HttpStatus.BAD_REQUEST, null, null);
    }

    @Override
    public SendVerificationResult resendVerification(HttpServletRequest request, Locale locale) throws MessagingException {
        SessionState sessionState = (SessionState) verificationStorage.getAttr(request, VerificationStorage.STATE, SessionState.NONE);
        AuthKey authKey = (AuthKey) verificationStorage.getAttr(request, VerificationStorage.AUTH_KEY, null);

        if (authKey == null || Objects.equals(sessionState, SessionState.NONE)) {
            return new SendVerificationResult(HttpStatus.BAD_REQUEST, null, null);
        }
        int code = codeGenerator.generate();
        long expiredAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2);

        Verification verification = new Verification();

        verification.setVerificationKey(authKey.getEmail());
        verification.setCode(String.valueOf(code));
        verification.setExipredAt(expiredAt);

        verificationDao.create(verification);

        emailService.sendEmail(
                authKey.getEmail(),
                getEmailSubject(request, locale),
                getEmailMessage(request, locale, String.valueOf(code))
        );

        return new SendVerificationResult(HttpStatus.OK, null, null);
    }

    private String getEmailSubject(HttpServletRequest request, Locale locale) {
        SessionState sessionState = (SessionState) coldVerificationStorage.getAttr(request, VerificationStorage.STATE, SessionState.NONE);

        if (Objects.equals(sessionState, SessionState.NONE)) {
            return null;
        }
        switch (sessionState) {
            case RESTORE_PASSWORD:
                return messageSource.getMessage("confirm.restorePassword.subject", new Object[]{}, locale);
            case CHANGE_EMAIL:
                return messageSource.getMessage("confirm.changeEmail.subject", new Object[]{}, locale);
            case SIGN_UP_CONFIRM:
                return messageSource.getMessage("confirm.signUp.subject", new Object[]{}, locale);
            case NONE:
                break;
        }

        return null;
    }

    private String getEmailMessage(HttpServletRequest request, Locale locale, String code) {
        SessionState sessionState = (SessionState) coldVerificationStorage.getAttr(request, VerificationStorage.STATE, SessionState.NONE);

        if (Objects.equals(sessionState, SessionState.NONE)) {
            return null;
        }

        switch (sessionState) {
            case RESTORE_PASSWORD: {
                String firstName = (String) coldVerificationStorage.getAttr(request, VerificationStorage.FIRST_NAME);

                return messageSource.getMessage(
                        "confirm.restorePassword.message",
                        new Object[]{firstName, code},
                        locale
                );
            }
            case CHANGE_EMAIL: {
                User user = (User) securityService.findLoggedInUser();

                return messageSource.getMessage(
                        "confirm.changeEmail.message",
                        new Object[]{user.getBiography().getFirstName(), code},
                        locale
                );
            }
            case SIGN_UP_CONFIRM: {
                SignUpRequest signUpRequest = (SignUpRequest) coldVerificationStorage.getAttr(request, VerificationStorage.SIGN_UP_REQUEST);

                return messageSource.getMessage(
                        "confirm.signUp.message",
                        new Object[]{signUpRequest.getFirstName(), code},
                        locale
                );
            }
            case NONE:
                break;
        }

        return null;
    }
}
