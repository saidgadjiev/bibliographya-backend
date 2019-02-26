package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.domain.EmailVerificationResult;
import ru.saidgadjiev.bibliographya.utils.TimeUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * Created by said on 24.02.2019.
 */
@Service
public class SessionEmailVerificationService {

    private final CodeGenerator codeGenerator;

    private final EmailService emailService;

    @Autowired
    public SessionEmailVerificationService(CodeGenerator codeGenerator, EmailService emailService) {
        this.codeGenerator = codeGenerator;
        this.emailService = emailService;
    }

    public void sendVerification(HttpServletRequest request, String email) {
        HttpSession session = request.getSession(true);
        int code = codeGenerator.generate();

        emailService.sendVerificationMessage(email, code);

        session.setAttribute("code", code);
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, 1);

        session.setAttribute("expiredAt", calendar.getTimeInMillis());
    }

    public EmailVerificationResult verify(HttpServletRequest request, String email, int code) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return new EmailVerificationResult().setStatus(EmailVerificationResult.Status.INVALID);
        }

        int currentCode = (int) session.getAttribute("code");
        long expiredAt = (long) session.getAttribute("expiredAt");

        if (TimeUtils.isExpired(expiredAt)) {
            return new EmailVerificationResult().setStatus(EmailVerificationResult.Status.EXPIRED);
        }

        boolean equals = Objects.equals(currentCode, code);

        return equals ? new EmailVerificationResult().setStatus(EmailVerificationResult.Status.VALID)
                : new EmailVerificationResult().setStatus(EmailVerificationResult.Status.INVALID);
    }

    public EmailVerificationResult confirm(HttpServletRequest request, String email, Integer code) {
        HttpSession session = request.getSession(false);
        EmailVerificationResult verificationResult = verify(request, email, code);

        if (verificationResult.isValid()) {
            session.removeAttribute("code");
            session.removeAttribute("expiredAt");
        }

        return verificationResult;
    }
}
