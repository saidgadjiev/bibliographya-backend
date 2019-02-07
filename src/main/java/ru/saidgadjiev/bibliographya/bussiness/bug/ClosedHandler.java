package ru.saidgadjiev.bibliographya.bussiness.bug;

import ru.saidgadjiev.bibliographya.bussiness.bug.operation.*;
import ru.saidgadjiev.bibliographya.dao.impl.BugDao;
import ru.saidgadjiev.bibliographya.domain.Bug;
import ru.saidgadjiev.bibliographya.domain.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class ClosedHandler implements Handler {

    private final BugDao bugDao;

    public ClosedHandler(BugDao bugDao) {
        this.bugDao = bugDao;
    }

    @Override
    public Bug handle(Signal signal, Map<String, Object> args) throws SQLException {
        switch (signal) {
            case PENDING:
                return new PendingOperation(bugDao).execute(args);
            case RELEASE:
                return new ReleaseOperation(bugDao).execute(args);
        }

        return null;
    }

    @Override
    public Collection<BugAction> getActions(Map<String, Object> args) {
        User user = (User) args.get("user");
        int fixerId = (Integer) args.get("fixerId");

        if (user.getId() == fixerId) {
            return new ArrayList<BugAction>() {{
                add(BugAction.pending());
                add(BugAction.release());
            }};
        }

        return Collections.emptyList();
    }
}
