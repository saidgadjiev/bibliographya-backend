package ru.saidgadjiev.bibliographya.bussiness.fix;

/**
 * Created by said on 21.12.2018.
 */
public class FixAction {

    private String name;

    private String caption;

    private String signal;

    private FixAction(String name, String caption, String signal) {
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

    public static FixAction assignMe() {
        return new FixAction("AssignMe", "Назначить меня", Handler.Signal.ASSIGN_ME.getDesc());
    }

    public static FixAction close() {
        return new FixAction("Close", "Закрыть", Handler.Signal.CLOSE.getDesc());
    }

    public static FixAction ignore() {
        return new FixAction("Ignore", "Не будет исправлено", Handler.Signal.IGNORE.getDesc());
    }

    public static FixAction release() {
        return new FixAction("Release", "Вернуть", Handler.Signal.RELEASE.getDesc());
    }
}
