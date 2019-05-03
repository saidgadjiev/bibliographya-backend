package ru.saidgadjiev.bibliographya.bussiness.fix.operation;

import ru.saidgadjiev.bibliographya.dao.impl.BiographyFixDao;
import ru.saidgadjiev.bibliographya.data.PreparedSetter;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.column.ColumnSpec;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.AndCondition;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.Equals;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.literals.Param;
import ru.saidgadjiev.bibliographya.domain.BiographyFix;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by said on 20.01.2019.
 */
public class IgnoreOperation {

    private final BiographyFixDao biographyFixDao;

    public IgnoreOperation(BiographyFixDao biographyFixDao) {
        this.biographyFixDao = biographyFixDao;
    }

    public BiographyFix execute(Map<String, Object> args) {
        List<UpdateValue> updateValues = new ArrayList<>();
        String fixInfo = (String) args.get("info");

        updateValues.add(
                new UpdateValue<>(
                        "info",
                        (preparedStatement, index) -> preparedStatement.setString(index, fixInfo)
                )
        );

        updateValues.add(
                new UpdateValue<>(
                        "status",
                        (preparedStatement, index) -> preparedStatement.setInt(index, BiographyFix.FixStatus.IGNORED.getCode())

                )
        );

        int id = (int) args.get("fixId");

        int fixerId = (Integer) args.get("fixerId");

        return biographyFixDao.update(updateValues, new AndCondition() {{
            add(new Equals(new ColumnSpec(BiographyFix.FIXER_ID), new Param()));
            add(new Equals(new ColumnSpec(BiographyFix.ID),  new Param()));
        }}, new ArrayList<PreparedSetter>() {{
            add((preparedStatement, index) -> preparedStatement.setInt(index, fixerId));
            add((preparedStatement, index) -> preparedStatement.setInt(index, id));
        }});
    }
}
