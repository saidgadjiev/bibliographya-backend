package ru.saidgadjiev.bibliographya.bussiness.moderation;

import ru.saidgadjiev.bibliographya.bussiness.moderation.operation.ApproveOperation;
import ru.saidgadjiev.bibliographya.bussiness.moderation.operation.AssignMeOperation;
import ru.saidgadjiev.bibliographya.bussiness.moderation.operation.RejectOperation;
import ru.saidgadjiev.bibliographya.bussiness.moderation.operation.ReleaseOperation;
import ru.saidgadjiev.bibliographya.dao.impl.BiographyModerationDao;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Created by said on 17.12.2018.
 */
public class PendingHandler implements Handler {

    private final BiographyModerationDao biographyModerationDao;

    public PendingHandler(BiographyModerationDao biographyModerationDao) {
        this.biographyModerationDao = biographyModerationDao;
    }

    @Override
    public Biography handle(Signal signal, Map<String, Object> args) throws SQLException {
        switch (signal) {
            case ASSIGN_ME:
                return new AssignMeOperation(biographyModerationDao).execute(args);
            case RELEASE:
                return new ReleaseOperation(biographyModerationDao).execute(args);
            case REJECT:
                return new RejectOperation(biographyModerationDao).execute(args);
            case APPROVE:
                return new ApproveOperation(biographyModerationDao).execute(args);
            default:
                return null;
        }
    }

    @Override
    public Collection<ModerationAction> getActions(Map<String, Object> args) {
        Integer moderatorId = (Integer) args.get("moderatorId");

        if (moderatorId == null) {
            return new ArrayList<ModerationAction>() {{
                add(ModerationAction.assignMe());
            }};
        }

        User user = (User) args.get("user");

        if (user.getId() == moderatorId) {
            return new ArrayList<ModerationAction>() {{
                add(ModerationAction.reject());
                add(ModerationAction.approve());
                add(ModerationAction.release());
            }};
        }

        return Collections.emptyList();
    }
}
