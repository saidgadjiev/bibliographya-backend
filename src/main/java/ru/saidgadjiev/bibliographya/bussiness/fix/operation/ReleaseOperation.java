package ru.saidgadjiev.bibliographya.bussiness.fix.operation;

import ru.saidgadjiev.bibliographya.dao.impl.BiographyFixDao;
import ru.saidgadjiev.bibliographya.data.PreparedSetter;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.column.ColumnSpec;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.AndCondition;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.Equals;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.literals.Param;
import ru.saidgadjiev.bibliographya.domain.BiographyFix;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by said on 21.12.2018.
 */
public class ReleaseOperation {

    private final BiographyFixDao biographyFixDao;

    public ReleaseOperation(BiographyFixDao biographyFixDao) {
        this.biographyFixDao = biographyFixDao;
    }

    public BiographyFix execute(Map<String, Object> args) {
        List<UpdateValue> values = new ArrayList<>();

        values.add(
                new UpdateValue<>(
                        "status",
                        (preparedStatement, index) -> preparedStatement.setInt(index, BiographyFix.FixStatus.PENDING.getCode())
                )
        );
        values.add(
                new UpdateValue<>(
                        "fixer_id",
                        (preparedStatement, index) -> preparedStatement.setNull(index, Types.INTEGER)
                )
        );

        int fixerId = (Integer) args.get("fixerId");

        int id = (int) args.get("fixId");

        return biographyFixDao.update(values, new AndCondition() {{
            add(new Equals(new ColumnSpec(BiographyFix.FIXER_ID), new Param()));
            add(new Equals(new ColumnSpec(BiographyFix.ID),  new Param()));
        }}, new ArrayList<PreparedSetter>() {{
            add((preparedStatement, index) -> preparedStatement.setInt(index, fixerId));
            add((preparedStatement, index) -> preparedStatement.setInt(index, id));
        }});
    }
}
