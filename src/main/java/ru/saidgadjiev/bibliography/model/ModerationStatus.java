package ru.saidgadjiev.bibliography.model;

/**
 * Created by said on 26.11.2018.
 */
public enum ModerationStatus {

    PENDING(0),

    APPROVED(1),

    REJECTED(2);

    private int code;

    ModerationStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ModerationStatus fromCode(int code) {
        for (ModerationStatus moderationStatus : values()) {
            if (moderationStatus.code == code) {
                return moderationStatus;
            }
        }

        return null;
    }
}
