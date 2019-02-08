package ru.saidgadjiev.bibliographya.bussiness.bug;

import java.util.Objects;

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

    @Override
    public String toString() {
        return "BugAction{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BugAction bugAction = (BugAction) o;
        return Objects.equals(name, bugAction.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public static BugAction close() {
        return new BugAction("Close", "Закрыть", Handler.Signal.CLOSE.getDesc());
    }

    public static BugAction assignMe() {
        return new BugAction("AssignMe", "Назначить меня", Handler.Signal.ASSIGN_ME.getDesc());
    }

    public static BugAction pending() {
        return new BugAction("Pending", "Открыть", Handler.Signal.PENDING.getDesc());
    }

    public static BugAction ignore() {
        return new BugAction("Ignore", "Закрыть без исправления", Handler.Signal.IGNORE.getDesc());
    }

    public static BugAction release() {
        return new BugAction("Release", "Вернуть", Handler.Signal.RELEASE.getDesc());
    }
}
