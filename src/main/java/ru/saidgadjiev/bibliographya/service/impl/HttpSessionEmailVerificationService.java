package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.domain.EmailVerificationResult;
import ru.saidgadjiev.bibliographya.model.SessionState;
import ru.saidgadjiev.bibliographya.service.api.EmailService;
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

    private HttpSessionManager httpSessionManager;

    private final CodeGenerator codeGenerator;

    private final EmailService emailService;

    @Autowired
    public HttpSessionEmailVerificationService(HttpSessionManager httpSessionManager,
                                               CodeGenerator codeGenerator,
                                               EmailService emailService) {
        this.httpSessionManager = httpSessionManager;
        this.codeGenerator = codeGenerator;
        this.emailService = emailService;
    }

    public HttpStatus sendVerification(HttpServletRequest request, Locale locale, String email) throws MessagingException {
        SessionState sessionState = httpSessionManager.getState(request);

        if (!Objects.equals(sessionState, SessionState.NONE)) {
            int code = codeGenerator.generate();

            Calendar calendar = Calendar.getInstance();

            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, 1);

            httpSessionManager.setCode(request, email, code, calendar.getTimeInMillis());

            emailService.sendEmail(
                    email,
                    httpSessionManager.getEmailSubject(request, locale),
                    httpSessionManager.getEmailMessage(request, locale)
            );

            return HttpStatus.OK;
        }

        return HttpStatus.BAD_REQUEST;
    }

    public EmailVerificationResult verify(HttpServletRequest request, String email, int code) {
        SessionState sessionState = httpSessionManager.getState(request);

        if (Objects.equals(sessionState, SessionState.NONE)) {
            return new EmailVerificationResult().setStatus(EmailVerificationResult.Status.INVALID);
        }

        String currentEmail = httpSessionManager.getEmail(request);

        if (!Objects.equals(currentEmail, email)) {
            return new EmailVerificationResult().setStatus(EmailVerificationResult.Status.INVALID);
        }

        int currentCode = httpSessionManager.getCode(request);
        long expiredAt = httpSessionManager.getExpiredAt(request);

        if (TimeUtils.isExpired(expiredAt)) {
            return new EmailVerificationResult().setStatus(EmailVerificationResult.Status.EXPIRED);
        }

        boolean equals = Objects.equals(currentCode, code);

        return equals ? new EmailVerificationResult().setStatus(EmailVerificationResult.Status.VALID)
                : new EmailVerificationResult().setStatus(EmailVerificationResult.Status.INVALID);
    }
}
