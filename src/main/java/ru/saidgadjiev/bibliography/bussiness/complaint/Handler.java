package ru.saidgadjiev.bibliography.bussiness.complaint;

import ru.saidgadjiev.bibliography.bussiness.fix.FixAction;
import ru.saidgadjiev.bibliography.domain.BiographyFix;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Created by said on 21.12.2018.
 */
public interface Handler {

    void handle(Signal signal, Map<String, Object> args) throws SQLException;

    Collection<ComplaintAction> getActions(Map<String, Object> args);

    enum Signal {

        ASSIGN_ME("assign-me"),
        RELEASE("release"),
        CONSIDER("consider");

        private String desc;

        Signal(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }

        public static Handler.Signal fromDesc(String signal) {
            for (Handler.Signal val: values()) {
                if (val.desc.equals(signal)) {
                    return val;
                }
            }

            return null;
        }
    }
}
