package ru.saidgadjiev.bibliography.bussiness.fix;

import org.apache.commons.lang.StringUtils;
import ru.saidgadjiev.bibliography.bussiness.fix.operation.AssignMeOperation;
import ru.saidgadjiev.bibliography.bussiness.fix.operation.CloseOperation;
import ru.saidgadjiev.bibliography.bussiness.fix.operation.ReleaseOperation;
import ru.saidgadjiev.bibliography.dao.BiographyFixDao;
import ru.saidgadjiev.bibliography.domain.BiographyFix;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
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
            case CLOSE:
                return new CloseOperation(biographyFixDao).execute(args);
            case RELEASE:
                return new ReleaseOperation(biographyFixDao).execute(args);
        }

        return null;
    }

    @Override
    public Collection<FixAction> getActions(Map<String, Object> args) {
        String fixerName = (String) args.get("fixerName");

        if (StringUtils.isBlank(fixerName)) {
            return new ArrayList<FixAction>() {{
                add(FixAction.assignMe());
            }};
        }

        return new ArrayList<FixAction>() {{
            add(FixAction.close());
            add(FixAction.release());
        }};
    }
}
