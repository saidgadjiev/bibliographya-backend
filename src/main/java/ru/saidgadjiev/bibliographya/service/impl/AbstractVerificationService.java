package ru.saidgadjiev.bibliographya.service.impl;

import ru.saidgadjiev.bibliographya.dao.api.VerificationDao;
import ru.saidgadjiev.bibliographya.domain.AuthKey;
import ru.saidgadjiev.bibliographya.domain.Verification;
import ru.saidgadjiev.bibliographya.domain.VerificationResult;
import ru.saidgadjiev.bibliographya.model.SessionState;
import ru.saidgadjiev.bibliographya.service.api.VerificationService;
import ru.saidgadjiev.bibliographya.service.api.VerificationStorage;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

public abstract class AbstractVerificationService implements VerificationService {

    protected VerificationStorage verificationStorage;

    protected VerificationDao verificationDao;

    public AbstractVerificationService(VerificationStorage verificationStorage, VerificationDao verificationDao) {
        this.verificationStorage = verificationStorage;
        this.verificationDao = verificationDao;
    }

    @Override
    public VerificationResult verify(HttpServletRequest request, int code, boolean confirm) {
        SessionState sessionState = (SessionState) verificationStorage.getAttr(request, VerificationStorage.STATE, SessionState.NONE);
        AuthKey authKey = (AuthKey) verificationStorage.getAttr(request, VerificationStorage.AUTH_KEY, null);

        if (authKey == null || Objects.equals(sessionState, SessionState.NONE)) {
            return new VerificationResult().setStatus(VerificationResult.Status.INVALID);
        }

        Verification verification = null;

        switch (authKey.getType()) {
            case PHONE:
                verification = verificationDao.get(authKey.formattedNumber(), String.valueOf(code));
                break;
            case EMAIL:
                verification = verificationDao.get(authKey.getEmail(), String.valueOf(code));
                break;
        }

        if (verification == null) {
            return new VerificationResult().setStatus(VerificationResult.Status.INVALID);
        }

        VerificationResult verificationResult = new VerificationResult().setStatus(VerificationResult.Status.VALID);

        if (confirm && verificationResult.isValid()) {
            verificationDao.remove(verification);
        }

        return verificationResult;
    }

}
