package ru.saidgadjiev.bibliographya.bussiness.fix.operation;

import ru.saidgadjiev.bibliographya.dao.impl.BiographyFixDao;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.column.ColumnSpec;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.AndCondition;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.Equals;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.IsNull;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.literals.Param;
import ru.saidgadjiev.bibliographya.domain.BiographyFix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by said on 21.12.2018.
 */
public class AssignMeOperation {

    private final BiographyFixDao biographyFixDao;

    public AssignMeOperation(BiographyFixDao biographyFixDao) {
        this.biographyFixDao = biographyFixDao;
    }

    public BiographyFix execute(Map<String, Object> args) {
        List<UpdateValue> values = new ArrayList<>();
        int fixerId = (Integer) args.get("fixerId");

        values.add(
                new UpdateValue<>(
                        "fixer_id",
                        (preparedStatement, index) -> preparedStatement.setInt(index, fixerId)
                )
        );

        int id = (int) args.get("fixId");

        return biographyFixDao.update(values, new AndCondition() {{
            add(new IsNull(new ColumnSpec(BiographyFix.FIXER_ID)));
            add(new Equals(new ColumnSpec(BiographyFix.ID),  new Param()));
        }}, Collections.singletonList((preparedStatement, index) -> preparedStatement.setInt(index, id)));
    }
}
