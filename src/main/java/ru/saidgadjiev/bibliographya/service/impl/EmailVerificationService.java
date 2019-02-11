package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.dao.impl.EmailVerificationDao;
import ru.saidgadjiev.bibliographya.domain.EmailVerification;
import ru.saidgadjiev.bibliographya.domain.EmailVerificationResult;
import ru.saidgadjiev.bibliographya.utils.TimeUtils;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

/**
 * Created by said on 11.02.2019.
 */
@Service
public class EmailVerificationService {

    private Random random = new Random();

    private EmailService emailService;

    private EmailVerificationDao emailVerificationDao;

    @Autowired
    public EmailVerificationService(EmailService emailService, EmailVerificationDao emailVerificationDao) {
        this.emailService = emailService;
        this.emailVerificationDao = emailVerificationDao;
    }

    public EmailVerificationResult sendAndVerify(String email, Integer code) {
        EmailVerification verification = emailVerificationDao.getByEmail(email);

        if (verification == null) {
            int nextCode = nextCode();

            emailService.sendVerificationMessage(email, nextCode);

            EmailVerification newVerification = new EmailVerification();
            Calendar calendar = Calendar.getInstance();

            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, 1);

            newVerification.setCode(nextCode);
            newVerification.setEmail(email);
            newVerification.setExpiredAt(new Timestamp(calendar.getTime().getTime()));

            emailVerificationDao.create(newVerification);

            return new EmailVerificationResult().setStatus(EmailVerificationResult.Status.INVALID);
        }

        if (TimeUtils.isExpired(verification.getExpiredAt().getTime())) {
            return new EmailVerificationResult().setStatus(EmailVerificationResult.Status.EXPIRED);
        }

        boolean equals = Objects.equals(verification.getCode(), code);

        if (equals) {
            emailVerificationDao.deleteByEmail(email);

            return new EmailVerificationResult().setStatus(EmailVerificationResult.Status.VALID);
        }

        return new EmailVerificationResult().setStatus(EmailVerificationResult.Status.INVALID);
    }
    
    public EmailVerificationResult verify(String email, int code) {
        EmailVerification emailVerification = emailVerificationDao.getByEmail(email);
        
        if (emailVerification == null) {
            return new EmailVerificationResult().setStatus(EmailVerificationResult.Status.INVALID);
        }
        
        if (TimeUtils.isExpired(emailVerification.getExpiredAt().getTime())) {
            return new EmailVerificationResult().setStatus(EmailVerificationResult.Status.EXPIRED);
        }

        boolean equals = Objects.equals(emailVerification.getCode(), code);

        return equals ? new EmailVerificationResult().setStatus(EmailVerificationResult.Status.VALID)
                : new EmailVerificationResult().setStatus(EmailVerificationResult.Status.INVALID);
    }

    public int resend(String email) {
        EmailVerification emailVerification = emailVerificationDao.getByEmail(email);
        
        if (emailVerification == null) {
            return 0;
        }
        if (TimeUtils.isExpired(emailVerification.getExpiredAt().getTime())) {
            int nextCode = nextCode();
            emailService.sendVerificationMessage(email, nextCode);

            emailVerificationDao.updateCode(email, nextCode);
        } else {
            emailService.sendVerificationMessage(email, emailVerification.getCode());
        }
        
        return 1;
    }

    private int nextCode() {
        return random.nextInt(9000) + 1000;
    }
}
