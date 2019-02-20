package ru.saidgadjiev.bibliographya.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.saidgadjiev.bibliographya.dao.impl.EmailVerificationDao;
import ru.saidgadjiev.bibliographya.domain.EmailVerification;
import ru.saidgadjiev.bibliographya.domain.EmailVerificationResult;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class EmailVerificationServiceTest {

    @MockBean
    private CodeGenerator codeGenerator;

    @MockBean
    private EmailService emailService;

    @MockBean
    private EmailVerificationDao verificationDao;

    @Autowired
    private EmailVerificationService verificationService;

    @Test
    void sendVerification() {
        List<EmailVerification> db = new ArrayList<>();

        Mockito.when(codeGenerator.generate()).thenReturn(1024);

        Mockito.doAnswer(invocation -> {
            EmailVerification emailVerification = (EmailVerification) invocation.getArguments()[0];

            emailVerification.setId(1);

            db.add(emailVerification);

            return null;
        }).when(verificationDao).create(any(EmailVerification.class));

        verificationService.sendVerification("test");

        Mockito.verify(emailService, Mockito.times(1)).sendVerificationMessage(eq("test"), anyInt());

        Assertions.assertEquals(1, db.size());
        Assertions.assertEquals(db.get(0).getEmail(), "test");
        Assertions.assertNotNull(db.get(0).getExpiredAt());
        Assertions.assertEquals(db.get(0).getCode(), 1024);
    }

    @Test
    void verify() {
        EmailVerification verification = createTestVerification();

        Mockito.when(codeGenerator.generate()).thenReturn(1024);
        Mockito.when(verificationDao.getByEmail(eq("test"))).thenReturn(verification);

        EmailVerificationResult verificationResult = verificationService.verify("test", 1024);

        Assertions.assertTrue(verificationResult.isValid());
    }

    @Test
    void confirm() {
        EmailVerification verification = createTestVerification();

        Mockito.when(codeGenerator.generate()).thenReturn(1024);
        Mockito.when(verificationDao.getByEmail(eq("test"))).thenReturn(verification);

        EmailVerificationResult verificationResult = verificationService.confirm("test", 1024);

        Assertions.assertTrue(verificationResult.isValid());
        Mockito.verify(verificationDao, Mockito.times(1)).deleteByEmail(eq("test"));
    }

    @Test
    void resend() {
        EmailVerification verification = createTestVerification();

        Mockito.when(verificationDao.getByEmail(eq("test"))).thenReturn(verification);

        verificationService.resend("test");

        Mockito.verify(emailService, Mockito.times(1)).sendVerificationMessage(eq("test"), eq(1024));
    }

    private EmailVerification createTestVerification() {
        EmailVerification verification = new EmailVerification();

        verification.setId(1);
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, 1);

        verification.setExpiredAt(new Timestamp(calendar.getTime().getTime()));
        verification.setEmail("test");
        verification.setCode(1024);

        return verification;
    }
}