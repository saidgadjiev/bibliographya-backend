package ru.saidgadjiev.bibliographya.domain;

/**
 * Created by said on 11.02.2019.
 */
public class SignUpResult {

    private EmailVerificationResult emailVerificationResult;

    public EmailVerificationResult getEmailVerificationResult() {
        return emailVerificationResult;
    }

    public SignUpResult setEmailVerificationResult(EmailVerificationResult emailVerificationResult) {
        this.emailVerificationResult = emailVerificationResult;

        return this;
    }
}
