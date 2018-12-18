package ru.saidgadjiev.bibliography.service.impl.moderation.handler;

import ru.saidgadjiev.bibliography.domain.Biography;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Created by said on 17.12.2018.
 */
public interface Handler {

    Biography handle(Signal signal, Map<String, Object> args) throws SQLException;

    Collection<ModerationAction> getActions(Map<String, Object> args);

    enum Signal {

        ASSIGN_ME("assign-me"),
        PENDING("pending"),
        APPROVE("approve"),
        RELEASE("release"),
        FIX("fix"),
        REJECT("reject");

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
