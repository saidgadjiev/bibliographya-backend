package ru.saidgadjiev.bibliography.bussiness.complaint;

import ru.saidgadjiev.bibliography.bussiness.complaint.operation.AssignMeOperation;
import ru.saidgadjiev.bibliography.bussiness.complaint.operation.ConsiderOperation;
import ru.saidgadjiev.bibliography.bussiness.complaint.operation.ReleaseOperation;
import ru.saidgadjiev.bibliography.bussiness.fix.FixAction;
import ru.saidgadjiev.bibliography.dao.BiographyReportDao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Created by said on 01.01.2019.
 */
public class PendingHandler implements Handler {

    private final BiographyReportDao reportDao;

    public PendingHandler(BiographyReportDao reportDao) {
        this.reportDao = reportDao;
    }

    @Override
    public void handle(Signal signal, Map<String, Object> args) throws SQLException {
        switch (signal) {
            case ASSIGN_ME:
                new AssignMeOperation(reportDao).execute(args);
                break;
            case RELEASE:
                new ReleaseOperation(reportDao).execute(args);
                break;
            case CONSIDER:
                new ConsiderOperation(reportDao).execute(args);
                break;
        }
    }

    @Override
    public Collection<ComplaintAction> getActions(Map<String, Object> args) {
        Integer considerId = (Integer) args.get("considerId");

        if (considerId == null) {
            return new ArrayList<ComplaintAction>() {{
                add(ComplaintAction.assignMe());
            }};
        }

        return new ArrayList<ComplaintAction>() {{
            add(ComplaintAction.consider());
            add(ComplaintAction.release());
        }};
    }
}
