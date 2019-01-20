package ru.saidgadjiev.bibliography.bussiness.fix;

import ru.saidgadjiev.bibliography.bussiness.fix.operation.AssignMeOperation;
import ru.saidgadjiev.bibliography.bussiness.fix.operation.CloseOperation;
import ru.saidgadjiev.bibliography.bussiness.fix.operation.IgnoreOperation;
import ru.saidgadjiev.bibliography.bussiness.fix.operation.ReleaseOperation;
import ru.saidgadjiev.bibliography.dao.impl.BiographyFixDao;
import ru.saidgadjiev.bibliography.domain.BiographyFix;
import ru.saidgadjiev.bibliography.domain.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Created by said on 21.12.2018.
 */
public class PendingHandler implements Handler {

    private final BiographyFixDao biographyFixDao;

    public PendingHandler(BiographyFixDao biographyFixDao) {
        this.biographyFixDao = biographyFixDao;
    }

    @Override
    public BiographyFix handle(Signal signal, Map<String, Object> args) throws SQLException {
        switch (signal) {
            case ASSIGN_ME:
                return new AssignMeOperation(biographyFixDao).execute(args);
            case IGNORE:
                return new IgnoreOperation(biographyFixDao).execute(args);
            case CLOSE:
                return new CloseOperation(biographyFixDao).execute(args);
            case RELEASE:
                return new ReleaseOperation(biographyFixDao).execute(args);
        }

        return null;
    }

    @Override
    public Collection<FixAction> getActions(Map<String, Object> args) {
        Integer fixerId = (Integer) args.get("fixerId");

        if (fixerId == null) {
            return new ArrayList<FixAction>() {{
                add(FixAction.assignMe());
            }};
        }

        User user = (User) args.get("user");

        if (user.getId() == fixerId) {
            return new ArrayList<FixAction>() {{
                add(FixAction.close());
                add(FixAction.release());
                add(FixAction.ignore());
            }};
        }

        return Collections.emptyList();
    }
}
