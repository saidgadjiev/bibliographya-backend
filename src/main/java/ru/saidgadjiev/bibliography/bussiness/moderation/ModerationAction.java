package ru.saidgadjiev.bibliography.bussiness.moderation;

/**
 * Created by said on 17.12.2018.
 */
public class ModerationAction {

    private String name;

    private String caption;

    private String signal;

    private ModerationAction(String name, String caption, String signal) {
        this.name = name;
        this.caption = caption;
        this.signal = signal;
    }

    public String getName() {
        return name;
    }

    public String getCaption() {
        return caption;
    }

    public String getSignal() {
        return signal;
    }

    public static ModerationAction reject() {
        return new ModerationAction("Reject", "Отклонить", Handler.Signal.REJECT.getDesc());
    }

    public static ModerationAction approve() {
        return new ModerationAction("Approve", "Принять", Handler.Signal.APPROVE.getDesc());
    }

    public static ModerationAction release() {
        return new ModerationAction("Release", "Вернуть", Handler.Signal.RELEASE.getDesc());
    }

    public static ModerationAction assignMe() {
        return new ModerationAction("AssignMe", "Назначить меня", Handler.Signal.ASSIGN_ME.getDesc());
    }

    public static ModerationAction pending() {
        return new ModerationAction("Pending", "На модерацию", Handler.Signal.PENDING.getDesc());
    }

    public static ModerationAction resend() {
        return new ModerationAction("Resend", "Отправить повторно", Handler.Signal.PENDING.getDesc());
    }
}
