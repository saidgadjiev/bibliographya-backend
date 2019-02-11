package ru.saidgadjiev.bibliographya.domain;

/**
 * Created by said on 11.02.2019.
 */
public class EmailVerificationResult {

    private Status status;

    public Status getStatus() {
        return status;
    }

    public EmailVerificationResult setStatus(Status status) {
        this.status = status;

        return this;
    }

    public boolean isValid() {
        return status == Status.VALID;
    }

    public boolean isExpired() {
        return status == Status.EXPIRED;
    }

    public boolean isInvalid() {
        return status == Status.INVALID;
    }

    public enum Status {

        EXPIRED(0),

        INVALID(1),

        VALID(2);

        private int code;

        Status(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}
