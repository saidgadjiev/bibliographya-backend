package ru.saidgadjiev.bibliographya.bussiness.moderation;

import ru.saidgadjiev.bibliographya.domain.Biography;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Created by said on 17.12.2018.
 */
public interface Handler {

    Biography handle(Signal signal, Map<String, Object> args) throws SQLException;

    Collection<ModerationAction> getActions(Map<String, Object> args);

    default Collection<ModerationAction> getUserActions(Map<String, Object> args) {
        return Collections.emptyList();
    }

    enum Signal {

        ASSIGN_ME("assign-me"),
        PENDING("pending"),
        APPROVE("approve"),
        RELEASE("release"),
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
