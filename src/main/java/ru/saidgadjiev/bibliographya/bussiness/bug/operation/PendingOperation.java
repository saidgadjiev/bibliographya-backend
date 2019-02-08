package ru.saidgadjiev.bibliographya.bussiness.bug.operation;

import ru.saidgadjiev.bibliographya.bussiness.common.BusinessOperation;
import ru.saidgadjiev.bibliographya.dao.impl.BugDao;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.FilterOperation;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.domain.Bug;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PendingOperation implements BusinessOperation<Bug> {

    private final BugDao bugDao;

    public PendingOperation(BugDao bugDao) {
        this.bugDao = bugDao;
    }

    @Override
    public Bug execute(Map<String, Object> args) {
        List<UpdateValue> values = new ArrayList<>();

        values.add(
                new UpdateValue<>(
                        "status",
                        Bug.BugStatus.PENDING.getCode(),
                        PreparedStatement::setInt
                )
        );

        List<FilterCriteria> criteria = new ArrayList<>();
        int bugId = (int) args.get("bugId");

        criteria.add(
                new FilterCriteria<>(
                        "id",
                        FilterOperation.EQ,
                        PreparedStatement::setInt,
                        bugId,
                        true
                )
        );

        int fixerId = (Integer) args.get("fixerId");

        criteria.add(
                new FilterCriteria<>(
                        "fixer_id",
                        FilterOperation.EQ,
                        PreparedStatement::setInt,
                        fixerId,
                        true
                )
        );
        Bug bug = bugDao.update(values, criteria);

        if (bug == null) {
            return null;
        }

        if (bugDao.getDialect().supportReturning()) {
            return bug;
        }

        return bugDao.getById(bugId);
    }
}
