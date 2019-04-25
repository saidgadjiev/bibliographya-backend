package ru.saidgadjiev.bibliographya.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.dao.api.VerificationDao;
import ru.saidgadjiev.bibliographya.domain.*;
import ru.saidgadjiev.bibliographya.model.SessionState;
import ru.saidgadjiev.bibliographya.model.SignUpRequest;
import ru.saidgadjiev.bibliographya.properties.JwtProperties;
import ru.saidgadjiev.bibliographya.service.api.EmailService;
import ru.saidgadjiev.bibliographya.service.api.TokenService;
import ru.saidgadjiev.bibliographya.service.api.VerificationStorage;
import ru.saidgadjiev.bibliographya.utils.TimeUtils;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Created by said on 25/04/2019.
 */
@Service
@Qualifier("email")
public class EmailVerificationService {

    private VerificationStorage coldVerificationStorage;

    private VerificationDao verificationDao;

    private final CodeGenerator codeGenerator;

    private final EmailService emailService;

    private MessageSource messageSource;

    private SecurityService securityService;

    private TokenService tokenService;

    @Autowired
    public EmailVerificationService(@Qualifier("cold") VerificationStorage coldVerificationStorage,
                                      VerificationDao verificationDao,
                                      CodeGenerator codeGenerator,
                                      EmailService emailService,
                                      MessageSource messageSource,
                                      SecurityService securityService,
                                      TokenService tokenService) {
        this.coldVerificationStorage = coldVerificationStorage;
        this.verificationDao = verificationDao;
        this.codeGenerator = codeGenerator;
        this.emailService = emailService;
        this.messageSource = messageSource;
        this.securityService = securityService;
        this.tokenService = tokenService;
    }

    public SentVerification sendVerification(HttpServletRequest request, Locale locale, VerificationKey verificationKey) throws MessagingException {
        SessionState sessionState = (SessionState) coldVerificationStorage.getAttr(request, VerificationStorage.STATE);

        if (!Objects.equals(sessionState, SessionState.NONE) && canSend(request)) {
            int code = codeGenerator.generate();
            long expiredAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2);

            Verification verification = new Verification();

            verification.setVerificationKey(verificationKey.getEmail());
            verification.setCode(String.valueOf(code));
            verification.setExipredAt(expiredAt);

            verificationDao.create(verification);

            emailService.sendEmail(
                    verificationKey.getEmail(),
                    getEmailSubject(request, locale),
                    getEmailMessage(request, locale)
            );

            String token = tokenService.generate(new HashMap<String, Object>() {{
                put("exp", expiredAt);
                put("timer", TimeUnit.MINUTES.toSeconds(2));
            }});

            return new SentVerification(HttpStatus.OK, token);
        }

        return new SentVerification(HttpStatus.BAD_REQUEST, null);
    }

    public VerificationResult verify(HttpServletRequest request, VerificationKey verificationKey, int code) {
        SessionState sessionState = (SessionState) coldVerificationStorage.getAttr(request, VerificationStorage.STATE);

        if (Objects.equals(sessionState, SessionState.NONE)) {
            return new VerificationResult().setStatus(VerificationResult.Status.INVALID);
        }

        Verification verification = verificationDao.get(verificationKey.getEmail(), String.valueOf(code));

        if (verification == null) {
            return new VerificationResult().setStatus(VerificationResult.Status.INVALID);
        }

        String currentEmail = verification.getVerificationKey();

        if (!Objects.equals(currentEmail, verificationKey.getEmail())) {
            return new VerificationResult().setStatus(VerificationResult.Status.INVALID);
        }

        int currentCode = (int) coldVerificationStorage.getAttr(request, VerificationStorage.CODE);

        boolean equals = Objects.equals(currentCode, code);

        return equals ? new VerificationResult().setStatus(VerificationResult.Status.VALID)
                : new VerificationResult().setStatus(VerificationResult.Status.INVALID);
    }

    private String getEmailSubject(HttpServletRequest request, Locale locale) {
        SessionState sessionState = (SessionState) coldVerificationStorage.getAttr(request, VerificationStorage.STATE);

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

    private String getEmailMessage(HttpServletRequest request, Locale locale) {
        SessionState sessionState = (SessionState) coldVerificationStorage.getAttr(request, VerificationStorage.STATE);

        if (sessionState == null) {
            return null;
        }
        String code = String.valueOf(coldVerificationStorage.getAttr(request, VerificationStorage.CODE));

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

    private boolean canSend(HttpServletRequest request) {
        String token = request.getHeader(JwtProperties.VERIFICATION_TOKEN);

        if (StringUtils.isBlank(token)) {
            return false;
        }
        Map<String, Object> claims = tokenService.validate(token);

        if (claims == null) {
            return false;
        }

        if (!TimeUtils.isExpired((Long) claims.get("exp"))) {
            return false;
        }

        return true;
    }
}
