package ru.saidgadjiev.bibliographya.bussiness.bug;

import ru.saidgadjiev.bibliographya.bussiness.bug.operation.AssignMeOperation;
import ru.saidgadjiev.bibliographya.bussiness.bug.operation.CloseOperation;
import ru.saidgadjiev.bibliographya.bussiness.bug.operation.IgnoreOperation;
import ru.saidgadjiev.bibliographya.bussiness.bug.operation.ReleaseOperation;
import ru.saidgadjiev.bibliographya.domain.Bug;
import ru.saidgadjiev.bibliographya.domain.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class PendingHandler implements Handler {

    private final AssignMeOperation assignMeOperation;
    private final CloseOperation closeOperation;
    private final IgnoreOperation ignoreOperation;
    private final ReleaseOperation releaseOperation;

    public PendingHandler(AssignMeOperation assignMeOperation,
                          CloseOperation closeOperation,
                          IgnoreOperation ignoreOperation,
                          ReleaseOperation releaseOperation) {
        this.assignMeOperation = assignMeOperation;
        this.closeOperation = closeOperation;
        this.ignoreOperation = ignoreOperation;
        this.releaseOperation = releaseOperation;
    }

    @Override
    public Bug handle(Signal signal, Map<String, Object> args) throws SQLException {
        switch (signal) {
            case ASSIGN_ME:
                return assignMeOperation.execute(args);
            case CLOSE:
                return closeOperation.execute(args);
            case IGNORE:
                return ignoreOperation.execute(args);
            case RELEASE:
                return releaseOperation.execute(args);
        }

        return null;
    }

    @Override
    public Collection<BugAction> getActions(Map<String, Object> args) {
        Integer fixerId = (Integer) args.get("fixerId");

        if (fixerId == null) {
            return new ArrayList<BugAction>() {{
                add(BugAction.assignMe());
            }};
        }

        User user = (User) args.get("user");

        if (user.getId() == fixerId) {
            return new ArrayList<BugAction>() {{
                add(BugAction.ignore());
                add(BugAction.close());
                add(BugAction.release());
            }};
        }

        return Collections.emptyList();
    }
}
