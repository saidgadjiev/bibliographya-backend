package ru.saidgadjiev.bibliographya.bussiness.bug;

import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.Bug;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Created by said on 17.12.2018.
 */
public interface Handler {

    Bug handle(Signal signal, Map<String, Object> args) throws SQLException;

    Collection<BugAction> getActions(Map<String, Object> args);

    enum Signal {

        ASSIGN_ME("assign-me"),
        OPEN("open"),
        CLOSE("close"),
        IGNORE("ignore"),
        RELEASE("release");

        private String desc;

        Signal(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }

        public static Signal fromDesc(String signal) {
            for (Signal val: values()) {
                if (val.desc.equals(signal)) {
                    return val;
                }
            }

            return null;
        }
    }
}
