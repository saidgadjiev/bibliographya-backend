package ru.saidgadjiev.bibliographya.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.dao.api.VerificationDao;
import ru.saidgadjiev.bibliographya.domain.AuthKey;
import ru.saidgadjiev.bibliographya.domain.SendVerificationResult;
import ru.saidgadjiev.bibliographya.domain.Timer;
import ru.saidgadjiev.bibliographya.domain.Verification;
import ru.saidgadjiev.bibliographya.model.SessionState;
import ru.saidgadjiev.bibliographya.properties.VerificationProperties;
import ru.saidgadjiev.bibliographya.service.api.PhoneService;
import ru.saidgadjiev.bibliographya.service.api.VerificationStorage;
import ru.saidgadjiev.bibliographya.utils.SecureUtils;
import ru.saidgadjiev.bibliographya.utils.TimeUtils;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Created by said on 23/04/2019.
 */
@Service
@Qualifier("phone")
public class PhoneVerificationService extends AbstractVerificationService {

    private PhoneService phoneService;

    private CodeGenerator codeGenerator;

    private VerificationProperties verificationProperties;

    public PhoneVerificationService(PhoneService phoneService,
                                    VerificationDao verificationDao,
                                    @Qualifier("inMemory") VerificationStorage verificationStorage,
                                    CodeGenerator codeGenerator,
                                    VerificationProperties verificationProperties) {
        super(verificationStorage, verificationDao);
        this.phoneService = phoneService;
        this.verificationProperties = verificationProperties;
        this.verificationDao = verificationDao;
        this.verificationStorage = verificationStorage;
        this.codeGenerator = codeGenerator;
    }

    @Override
    public SendVerificationResult sendVerification(HttpServletRequest request,
                                                   Locale locale,
                                                   AuthKey authKey) throws MessagingException {
        SessionState sessionState = (SessionState) verificationStorage.getAttr(request, VerificationStorage.STATE, SessionState.NONE);

        if (!Objects.equals(sessionState, SessionState.NONE) && checkTimer(request)) {
            int code = codeGenerator.generate();
            long expiredAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10);

            Verification verification = new Verification();

            verification.setVerificationKey(authKey.formattedNumber());
            verification.setCode(String.valueOf(code));
            verification.setExipredAt(expiredAt);

            verificationDao.create(verification);

            phoneService.sendSms(
                    authKey.formattedNumber(),
                    "Ваш код " + code
            );

            Timer timer = new Timer();

            timer.setExpiredAt(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(verificationProperties.getTime()));
            timer.setTime(verificationProperties.getTime());

            verificationStorage.setAttr(request, VerificationStorage.TIMER, timer);
            verificationStorage.setAttr(request, VerificationStorage.AUTH_KEY, authKey);

            return new SendVerificationResult(HttpStatus.OK, timer, SecureUtils.securePhone(authKey.formattedNumber()));
        }
        Timer timer = (Timer) verificationStorage.getAttr(request, VerificationStorage.TIMER, null);

        Timer exist = new Timer();

        long expiredAt = timer.getExpiredAt();
        long now = System.currentTimeMillis();

        exist.setTime(TimeUnit.MILLISECONDS.toSeconds(expiredAt - now));

        return new SendVerificationResult(HttpStatus.BAD_REQUEST, exist, null);
    }

    @Override
    public SendVerificationResult resendVerification(HttpServletRequest request, Locale locale) throws MessagingException {
        SessionState sessionState = (SessionState) verificationStorage.getAttr(request, VerificationStorage.STATE, SessionState.NONE);
        AuthKey authKey = (AuthKey) verificationStorage.getAttr(request, VerificationStorage.AUTH_KEY, null);

        if (authKey == null || Objects.equals(sessionState, SessionState.NONE)) {
            return new SendVerificationResult(HttpStatus.BAD_REQUEST, null, null);
        }

        if (checkTimer(request)) {
            int code = codeGenerator.generate();
            long expiredAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10);

            Verification verification = new Verification();

            verification.setVerificationKey(authKey.formattedNumber());
            verification.setCode(String.valueOf(code));
            verification.setExipredAt(expiredAt);

            verificationDao.create(verification);

            phoneService.sendSms(authKey.formattedNumber(), "Ваш код " + code);

            Timer timer = new Timer();

            timer.setExpiredAt(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(verificationProperties.getTime()));
            timer.setTime(verificationProperties.getTime());

            verificationStorage.setAttr(request, VerificationStorage.TIMER, timer);

            return new SendVerificationResult(HttpStatus.OK, timer, null);
        }
        Timer timer = (Timer) verificationStorage.getAttr(request, VerificationStorage.TIMER, null);

        Timer exist = new Timer();

        long expiredAt = timer.getExpiredAt();
        long now = System.currentTimeMillis();

        exist.setTime(TimeUnit.MILLISECONDS.toSeconds(expiredAt - now));

        return new SendVerificationResult(HttpStatus.BAD_REQUEST, exist, null);
    }

    private boolean checkTimer(HttpServletRequest request) {
        Timer timer = (Timer) verificationStorage.getAttr(request, VerificationStorage.TIMER, null);

        if (timer == null) {
            return true;
        }

        if (!TimeUtils.isExpired(timer.getExpiredAt())) {
            return false;
        }

        return true;
    }
}
