package ru.saidgadjiev.bibliographya.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.dao.api.VerificationDao;
import ru.saidgadjiev.bibliographya.domain.SendVerificationResult;
import ru.saidgadjiev.bibliographya.domain.Verification;
import ru.saidgadjiev.bibliographya.domain.AuthenticationKey;
import ru.saidgadjiev.bibliographya.model.SessionState;
import ru.saidgadjiev.bibliographya.properties.JwtProperties;
import ru.saidgadjiev.bibliographya.service.api.PhoneService;
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
 * Created by said on 23/04/2019.
 */
@Service
@Qualifier("phone")
public class PhoneVerificationService extends AbstractVerificationService {

    private PhoneService phoneService;

    private CodeGenerator codeGenerator;

    private TokenService tokenService;

    public PhoneVerificationService(PhoneService phoneService,
                                    VerificationDao verificationDao,
                                    @Qualifier("inMemory") VerificationStorage verificationStorage,
                                    CodeGenerator codeGenerator,
                                    TokenService tokenService) {
        super(verificationStorage, verificationDao);
        this.phoneService = phoneService;
        this.verificationDao = verificationDao;
        this.verificationStorage = verificationStorage;
        this.codeGenerator = codeGenerator;
        this.tokenService = tokenService;
    }

    @Override
    public SendVerificationResult sendVerification(HttpServletRequest request, Locale locale, AuthenticationKey authenticationKey) throws MessagingException {
        SessionState sessionState = (SessionState) verificationStorage.getAttr(request, VerificationStorage.STATE);

        if (!Objects.equals(sessionState, SessionState.NONE) && canSend(request)) {
            int code = codeGenerator.generate();
            long expiredAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2);

            Verification verification = new Verification();

            verification.setVerificationKey(authenticationKey.getEmail());
            verification.setCode(String.valueOf(code));
            verification.setExipredAt(expiredAt);

            verificationDao.create(verification);

            phoneService.sendSms(
                    authenticationKey.getCountryCode() + authenticationKey.getPhone(),
                    "Ваш код " + code
            );

            String token = tokenService.generate(new HashMap<String, Object>() {{
                put("exp", expiredAt);
                put("timer", TimeUnit.MINUTES.toSeconds(2));
            }});

            return new SendVerificationResult(HttpStatus.OK, token);
        }

        return new SendVerificationResult(HttpStatus.BAD_REQUEST, null);
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
