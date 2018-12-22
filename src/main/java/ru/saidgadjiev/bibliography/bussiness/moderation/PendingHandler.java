package ru.saidgadjiev.bibliography.bussiness.moderation;

import org.apache.commons.lang.StringUtils;
import ru.saidgadjiev.bibliography.bussiness.moderation.operation.ApproveOperation;
import ru.saidgadjiev.bibliography.bussiness.moderation.operation.AssignMeOperation;
import ru.saidgadjiev.bibliography.bussiness.moderation.operation.RejectOperation;
import ru.saidgadjiev.bibliography.bussiness.moderation.operation.ReleaseOperation;
import ru.saidgadjiev.bibliography.dao.BiographyModerationDao;
import ru.saidgadjiev.bibliography.domain.Biography;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
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
        String moderatorName = (String) args.get("moderatorName");

        if (StringUtils.isBlank(moderatorName)) {
            return new ArrayList<ModerationAction>() {{
                add(ModerationAction.assignMe());
            }};
        }

        return new ArrayList<ModerationAction>() {{
            add(ModerationAction.reject());
            add(ModerationAction.approve());
            add(ModerationAction.release());
        }};
    }
}
