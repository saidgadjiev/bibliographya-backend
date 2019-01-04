package ru.saidgadjiev.bibliography.bussiness.moderation;

import ru.saidgadjiev.bibliography.bussiness.moderation.operation.PendingOperation;
import ru.saidgadjiev.bibliography.bussiness.moderation.operation.RejectOperation;
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
public class ApprovedHandler implements Handler {

    private final BiographyModerationDao biographyModerationDao;

    public ApprovedHandler(BiographyModerationDao biographyModerationDao) {
        this.biographyModerationDao = biographyModerationDao;
    }

    @Override
    public Biography handle(Signal signal, Map<String, Object> args) throws SQLException {
        switch (signal) {
            case REJECT:
                return new RejectOperation(biographyModerationDao).execute(args);
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
                add(ModerationAction.reject());
                add(ModerationAction.pending());
                add(ModerationAction.release());
            }};
        }

        return Collections.emptyList();
    }
}
