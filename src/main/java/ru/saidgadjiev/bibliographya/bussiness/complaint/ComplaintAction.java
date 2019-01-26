package ru.saidgadjiev.bibliographya.bussiness.complaint;

/**
 * Created by said on 01.01.2019.
 */
public class ComplaintAction {

    private String name;

    private String caption;

    private String signal;

    private ComplaintAction(String name, String caption, String signal) {
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

    public static ComplaintAction assignMe() {
        return new ComplaintAction("AssignMe", "Назначить меня", Handler.Signal.ASSIGN_ME.getDesc());
    }

    public static ComplaintAction consider() {
        return new ComplaintAction("Consider", "Рассмотреть", Handler.Signal.CONSIDER.getDesc());
    }

    public static ComplaintAction release() {
        return new ComplaintAction("Release", "Вернуть", Handler.Signal.RELEASE.getDesc());
    }
}
