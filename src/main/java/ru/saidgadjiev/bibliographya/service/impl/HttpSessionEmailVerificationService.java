package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.domain.EmailVerificationResult;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.model.SessionState;
import ru.saidgadjiev.bibliographya.model.SignUpRequest;
import ru.saidgadjiev.bibliographya.service.api.EmailService;
import ru.saidgadjiev.bibliographya.service.api.VerificationStorage;
import ru.saidgadjiev.bibliographya.service.impl.verification.SessionVerificationStorage;
import ru.saidgadjiev.bibliographya.utils.TimeUtils;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * Created by said on 24.02.2019.
 */
@Service
public class HttpSessionEmailVerificationService {

    private VerificationStorage verificationStorage;

    private final CodeGenerator codeGenerator;

    private final EmailService emailService;

    private MessageSource messageSource;

    private SecurityService securityService;

    @Autowired
    public HttpSessionEmailVerificationService(SessionVerificationStorage verificationStorage,
                                               CodeGenerator codeGenerator,
                                               EmailService emailService,
                                               MessageSource messageSource,
                                               SecurityService securityService) {
        this.verificationStorage = verificationStorage;
        this.codeGenerator = codeGenerator;
        this.emailService = emailService;
        this.messageSource = messageSource;
        this.securityService = securityService;
    }

    public HttpStatus sendVerification(HttpServletRequest request, Locale locale, String email) throws MessagingException {
        SessionState sessionState = verificationStorage.getState(request);

        if (!Objects.equals(sessionState, SessionState.NONE)) {
            int code = codeGenerator.generate();

            Calendar calendar = Calendar.getInstance();

            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, 1);

            verificationStorage.setCode(request, email, code, calendar.getTimeInMillis());

            emailService.sendEmail(
                    email,
                    getEmailSubject(request, locale),
                    getEmailMessage(request, locale)
            );

            return HttpStatus.OK;
        }

        return HttpStatus.BAD_REQUEST;
    }

    public EmailVerificationResult verify(HttpServletRequest request, String email, int code) {
        SessionState sessionState = verificationStorage.getState(request);

        if (Objects.equals(sessionState, SessionState.NONE)) {
            return new EmailVerificationResult().setStatus(EmailVerificationResult.Status.INVALID);
        }

        String currentEmail = verificationStorage.getEmail(request);

        if (!Objects.equals(currentEmail, email)) {
            return new EmailVerificationResult().setStatus(EmailVerificationResult.Status.INVALID);
        }

        int currentCode = (int) verificationStorage.getAttr(request, VerificationStorage.CODE);
        long expiredAt = verificationStorage.getExpiredAt(request);

        if (TimeUtils.isExpired(expiredAt)) {
            return new EmailVerificationResult().setStatus(EmailVerificationResult.Status.EXPIRED);
        }

        boolean equals = Objects.equals(currentCode, code);

        return equals ? new EmailVerificationResult().setStatus(EmailVerificationResult.Status.VALID)
                : new EmailVerificationResult().setStatus(EmailVerificationResult.Status.INVALID);
    }

    public String getEmailSubject(HttpServletRequest request, Locale locale) {
        SessionState sessionState = (SessionState) verificationStorage.getAttr(request, VerificationStorage.STATE);

        if (sessionState == null) {
            return null;
        }
        switch (sessionState) {
            case RESTORE_PASSWORD:
                return messageSource.getMessage("confirm.restorePassword.subject", new Object[] {}, locale);
            case CHANGE_EMAIL:
                return messageSource.getMessage("confirm.changeEmail.subject", new Object[] {}, locale);
            case SIGN_UP_CONFIRM:
                return messageSource.getMessage("confirm.signUp.subject", new Object[] {}, locale);
            case NONE:
                break;
        }

        return null;
    }

    public String getEmailMessage(HttpServletRequest request, Locale locale) {
        SessionState sessionState = (SessionState) verificationStorage.getAttr(request, VerificationStorage.STATE);

        if (sessionState == null) {
            return null;
        }
        String code = String.valueOf(verificationStorage.getAttr(request, VerificationStorage.CODE));

        switch (sessionState) {
            case RESTORE_PASSWORD: {
                String firstName = (String) verificationStorage.getAttr(request, VerificationStorage.FIRST_NAME);

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
                SignUpRequest signUpRequest = (SignUpRequest) verificationStorage.getAttr(request, VerificationStorage.SIGN_UP_REQUEST);

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
