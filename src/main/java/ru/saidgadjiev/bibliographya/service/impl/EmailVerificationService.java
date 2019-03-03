package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.saidgadjiev.bibliographya.dao.impl.EmailVerificationDao;
import ru.saidgadjiev.bibliographya.domain.EmailVerification;
import ru.saidgadjiev.bibliographya.domain.EmailVerificationResult;
import ru.saidgadjiev.bibliographya.utils.TimeUtils;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * Created by said on 11.02.2019.
 */
@SuppressWarnings("PMD")
public class EmailVerificationService {

    private CodeGenerator codeGenerator;

    private EmailService emailService;

    private EmailVerificationDao emailVerificationDao;

    @Autowired
    public EmailVerificationService(CodeGenerator codeGenerator, EmailService emailService, EmailVerificationDao emailVerificationDao) {
        this.codeGenerator = codeGenerator;
        this.emailService = emailService;
        this.emailVerificationDao = emailVerificationDao;
    }

    @Transactional(rollbackFor = Exception.class)
    public void sendVerification(String email) {
        EmailVerification verification = emailVerificationDao.getByEmail(email);

        if (verification == null) {
            int nextCode = codeGenerator.generate();

            emailService.sendEmail(null, null, null);

            EmailVerification newVerification = new EmailVerification();
            Calendar calendar = Calendar.getInstance();

            calendar.setTime(new Date());
            calendar.add(Calendar.DATE, 1);

            newVerification.setCode(nextCode);
            newVerification.setEmail(email);
            newVerification.setExpiredAt(new Timestamp(calendar.getTime().getTime()));

            emailVerificationDao.create(newVerification);
        } else {
            resendCode(verification);
        }
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

    public EmailVerificationResult confirm(String email, Integer code) {
        EmailVerificationResult verificationResult = verify(email, code);

        if (verificationResult.isValid()) {
            emailVerificationDao.deleteByEmail(email);
        }

        return verificationResult;
    }

    public int resend(String email) {
        EmailVerification emailVerification = emailVerificationDao.getByEmail(email);

        if (emailVerification == null) {
            return 0;
        }

        resendCode(emailVerification);

        return 1;
    }

    private void resendCode(EmailVerification emailVerification) {
        if (TimeUtils.isExpired(emailVerification.getExpiredAt().getTime())) {
            int nextCode = codeGenerator.generate();
            emailService.sendEmail(null, null, null);

            emailVerificationDao.updateCode(emailVerification.getEmail(), nextCode);
        } else {
            emailService.sendEmail(null, null, null);
        }
    }
}
