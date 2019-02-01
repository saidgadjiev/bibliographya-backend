package ru.saidgadjiev.bibliographya.bussiness.bug.operation;

import ru.saidgadjiev.bibliographya.dao.impl.BugDao;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.FilterOperation;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.domain.Bug;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IgnoreOperation {

    private final BugDao bugDao;

    public IgnoreOperation(BugDao bugDao) {
        this.bugDao = bugDao;
    }

    public Bug execute(Map<String, Object> args) {
        List<UpdateValue> updateValues = new ArrayList<>();
        String info = (String) args.get("info");

        updateValues.add(
                new UpdateValue<>(
                        "info",
                        info,
                        PreparedStatement::setString
                )
        );

        updateValues.add(
                new UpdateValue<>(
                        "status",
                        Bug.BugStatus.IGNORED.getCode(),
                        PreparedStatement::setInt

                )
        );

        List<FilterCriteria> criteria = new ArrayList<>();
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

        return bugDao.update(updateValues, criteria);
    }
}
