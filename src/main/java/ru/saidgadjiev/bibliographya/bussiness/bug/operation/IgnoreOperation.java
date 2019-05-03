package ru.saidgadjiev.bibliographya.bussiness.bug.operation;

import ru.saidgadjiev.bibliographya.bussiness.common.BusinessOperation;
import ru.saidgadjiev.bibliographya.dao.impl.BugDao;
import ru.saidgadjiev.bibliographya.data.PreparedSetter;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.column.ColumnSpec;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.AndCondition;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.Equals;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.literals.Param;
import ru.saidgadjiev.bibliographya.domain.Bug;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class IgnoreOperation implements BusinessOperation<Bug> {

    private final BugDao bugDao;

    public IgnoreOperation(BugDao bugDao) {
        this.bugDao = bugDao;
    }

    @Override
    public Bug execute(Map<String, Object> args) {
        List<UpdateValue> updateValues = new ArrayList<>();
        String info = (String) args.get("info");

        updateValues.add(
                new UpdateValue<>(
                        "info",
                        (preparedStatement, index) -> preparedStatement.setString(index, info)
                )
        );

        updateValues.add(
                new UpdateValue<>(
                        "status",
                        (preparedStatement, index) -> preparedStatement.setInt(index, Bug.BugStatus.IGNORED.getCode())

                )
        );

        int id = (int) args.get("bugId");

        int fixerId = (Integer) args.get("fixerId");

        Bug bug = bugDao.update(updateValues, new AndCondition() {{
            add(new Equals(new ColumnSpec(Bug.FIXER_ID), new Param()));
            add(new Equals(new ColumnSpec(Bug.ID), new Param()));
        }}, new ArrayList<PreparedSetter>() {{
            add((preparedStatement, index) -> preparedStatement.setInt(index, fixerId));
            add((preparedStatement, index) -> preparedStatement.setInt(index, id));
        }});

        if (bug == null) {
            return null;
        }

        if (bugDao.getDialect().supportReturning()) {
            return bug;
        }

        return bugDao.getById((TimeZone) args.get("timeZone"), id);
    }
}
