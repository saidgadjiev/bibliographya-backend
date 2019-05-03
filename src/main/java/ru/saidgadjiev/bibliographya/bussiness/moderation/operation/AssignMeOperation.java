package ru.saidgadjiev.bibliographya.bussiness.moderation.operation;

import ru.saidgadjiev.bibliographya.dao.impl.BiographyModerationDao;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.column.ColumnSpec;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.AndCondition;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.Equals;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.IsNull;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.literals.Param;
import ru.saidgadjiev.bibliographya.domain.Biography;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by said on 17.12.2018.
 */
public class AssignMeOperation {

    private final BiographyModerationDao biographyModerationDao;

    public AssignMeOperation(BiographyModerationDao biographyModerationDao) {
        this.biographyModerationDao = biographyModerationDao;
    }

    public Biography execute(Map<String, Object> args) throws SQLException {
        int biographyId = (int) args.get("biographyId");
        Integer moderatorId = (Integer) args.get("moderatorId");
        List<UpdateValue> values = new ArrayList<>();

        values.add(
                new UpdateValue<>(
                        "moderator_id",
                        (preparedStatement, index) -> preparedStatement.setInt(index, moderatorId)
                )
        );

        return biographyModerationDao.update(values, new AndCondition() {{
            add(new Equals(new ColumnSpec(Biography.ID), new Param()));
            add(new IsNull(new ColumnSpec(Biography.MODERATOR_ID)));
        }}, Collections.singletonList(
                (preparedStatement, index) -> preparedStatement.setInt(index, biographyId)
        ));
    }
}
