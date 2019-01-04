package ru.saidgadjiev.bibliography.bussiness.moderation;

import ru.saidgadjiev.bibliography.bussiness.moderation.operation.ApproveOperation;
import ru.saidgadjiev.bibliography.bussiness.moderation.operation.PendingOperation;
import ru.saidgadjiev.bibliography.bussiness.moderation.operation.ReleaseOperation;
import ru.saidgadjiev.bibliography.dao.BiographyModerationDao;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Created by said on 17.12.2018.
 */
public class RejectedHandler implements Handler {

    private final BiographyModerationDao biographyModerationDao;

    public RejectedHandler(BiographyModerationDao biographyModerationDao) {
        this.biographyModerationDao = biographyModerationDao;
    }

    @Override
    public Biography handle(Signal signal, Map<String, Object> args) throws SQLException {
        switch (signal) {
            case APPROVE:
                return new ApproveOperation(biographyModerationDao).execute(args);
            case PENDING:
                return new PendingOperation(biographyModerationDao).execute(args);
            case RELEASE:
                return new ReleaseOperation(biographyModerationDao).execute(args);
            default:
                return null;
        }
    }

    @Override
    public Collection<ModerationAction> getActions(Map<String, Object> args) {
        User user = (User) args.get("user");
        int moderatorId = (Integer) args.get("moderatorId");

        if (user.getId() == moderatorId) {
            return new ArrayList<ModerationAction>() {{
                add(ModerationAction.approve());
                add(ModerationAction.pending());
                add(ModerationAction.release());
            }};
        }

        return Collections.emptyList();
    }

    @Override
    public Collection<ModerationAction> getUserActions(Map<String, Object> args) {
        return new ArrayList<ModerationAction>() {{
            add(ModerationAction.pending());
        }};
    }
}
