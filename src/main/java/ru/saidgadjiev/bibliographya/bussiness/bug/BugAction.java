package ru.saidgadjiev.bibliographya.bussiness.bug;

public class BugAction {

    private String name;

    private String caption;

    private String signal;

    private BugAction(String name, String caption, String signal) {
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

    public static BugAction close() {
        return new BugAction("Close", "Закрыть", Handler.Signal.CLOSE.getDesc());
    }

    public static BugAction assignMe() {
        return new BugAction("AssignMe", "Назначить меня", Handler.Signal.ASSIGN_ME.getDesc());
    }

    public static BugAction open() {
        return new BugAction("Open", "Открыть", Handler.Signal.OPEN.getDesc());
    }

    public static BugAction ignore() {
        return new BugAction("Ignore", "Закрыть без исправления", Handler.Signal.IGNORE.getDesc());
    }

    public static BugAction release() {
        return new BugAction("Release", "Вернуть", Handler.Signal.RELEASE.getDesc());
    }
}
