package ru.saidgadjiev.bibliographya.bussiness.bug.operation;

import ru.saidgadjiev.bibliographya.dao.impl.BugDao;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.FilterOperation;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.domain.BiographyFix;
import ru.saidgadjiev.bibliographya.domain.Bug;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CloseOperation {

    private final BugDao bugDao;

    public CloseOperation(BugDao bugDao) {
        this.bugDao = bugDao;
    }

    public Bug execute(Map<String, Object> args) {
        List<UpdateValue> values = new ArrayList<>();

        values.add(
                new UpdateValue<>(
                        "status",
                        Bug.BugStatus.CLOSED.getCode(),
                        PreparedStatement::setInt
                )
        );

        List<FilterCriteria> criteria = new ArrayList<>();
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

        return bugDao.update(values, criteria);
    }
}
