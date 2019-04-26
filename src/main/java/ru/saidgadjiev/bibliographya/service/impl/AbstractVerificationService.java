package ru.saidgadjiev.bibliographya.service.impl;

import ru.saidgadjiev.bibliographya.dao.api.VerificationDao;
import ru.saidgadjiev.bibliographya.domain.Verification;
import ru.saidgadjiev.bibliographya.domain.AuthenticationKey;
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
    public VerificationResult verify(HttpServletRequest request, AuthenticationKey authenticationKey, int code) {
        SessionState sessionState = (SessionState) verificationStorage.getAttr(request, VerificationStorage.STATE);

        if (Objects.equals(sessionState, SessionState.NONE)) {
            return new VerificationResult().setStatus(VerificationResult.Status.INVALID);
        }

        Verification verification = verificationDao.get(authenticationKey.getEmail(), String.valueOf(code));

        if (verification == null) {
            return new VerificationResult().setStatus(VerificationResult.Status.INVALID);
        }

        String currentEmail = verification.getVerificationKey();

        if (!Objects.equals(currentEmail, authenticationKey.getEmail())) {
            return new VerificationResult().setStatus(VerificationResult.Status.INVALID);
        }

        int currentCode = (int) verificationStorage.getAttr(request, VerificationStorage.CODE);

        boolean equals = Objects.equals(currentCode, code);

        return equals ? new VerificationResult().setStatus(VerificationResult.Status.VALID)
                : new VerificationResult().setStatus(VerificationResult.Status.INVALID);
    }

}
