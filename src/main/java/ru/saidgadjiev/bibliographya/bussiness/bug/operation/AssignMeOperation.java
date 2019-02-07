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

public class AssignMeOperation implements BusinessOperation<Bug> {

    private final BugDao bugDao;

    public AssignMeOperation(BugDao bugDao) {
        this.bugDao = bugDao;
    }

    @Override
    public Bug execute(Map<String, Object> args) {
        List<UpdateValue> values = new ArrayList<>();
        int fixerId = (Integer) args.get("fixerId");

        values.add(
                new UpdateValue<>(
                        "fixer_id",
                        fixerId,
                        PreparedStatement::setInt
                )
        );

        List<FilterCriteria> criteria = new ArrayList<>();

        criteria.add(
                new FilterCriteria<>(
                        "fixer_id",
                        FilterOperation.IS_NULL,
                        null,
                        null,
                        false
                )
        );

        int id = (int) args.get("bugId");

        criteria.add(
                new FilterCriteria<>(
                        "id",
                        FilterOperation.EQ,
                        PreparedStatement::setInt,
                        id,
                        true
                )
        );

        Bug bug = bugDao.update(values, criteria);

        if (bugDao.getDialect().supportReturning()) {
            return bug;
        }

        return bugDao.getById(id);
    }
}
