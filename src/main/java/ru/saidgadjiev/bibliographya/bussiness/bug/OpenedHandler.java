package ru.saidgadjiev.bibliographya.bussiness.bug;

import ru.saidgadjiev.bibliographya.bussiness.bug.operation.AssignMeOperation;
import ru.saidgadjiev.bibliographya.bussiness.bug.operation.CloseOperation;
import ru.saidgadjiev.bibliographya.bussiness.bug.operation.IgnoreOperation;
import ru.saidgadjiev.bibliographya.bussiness.bug.operation.ReleaseOperation;
import ru.saidgadjiev.bibliographya.bussiness.moderation.ModerationAction;
import ru.saidgadjiev.bibliographya.dao.impl.BugDao;
import ru.saidgadjiev.bibliographya.domain.Bug;
import ru.saidgadjiev.bibliographya.domain.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class OpenedHandler implements Handler {

    private final BugDao bugDao;

    public OpenedHandler(BugDao bugDao) {
        this.bugDao = bugDao;
    }

    @Override
    public Bug handle(Signal signal, Map<String, Object> args) throws SQLException {
        switch (signal) {
            case ASSIGN_ME:
                return new AssignMeOperation(bugDao).execute(args);
            case CLOSE:
                return new CloseOperation(bugDao).execute(args);
            case IGNORE:
                return new IgnoreOperation(bugDao).execute(args);
            case RELEASE:
                return new ReleaseOperation(bugDao).execute(args);
        }

        return null;
    }

    @Override
    public Collection<BugAction> getActions(Map<String, Object> args) {
        Integer userId = (Integer) args.get("userId");

        if (userId == null) {
            return new ArrayList<BugAction>() {{
                add(BugAction.assignMe());
            }};
        }

        User user = (User) args.get("user");

        if (user.getId() == userId) {
            return new ArrayList<BugAction>() {{
                add(BugAction.close());
                add(BugAction.ignore());
                add(BugAction.release());
            }};
        }

        return Collections.emptyList();
    }
}
