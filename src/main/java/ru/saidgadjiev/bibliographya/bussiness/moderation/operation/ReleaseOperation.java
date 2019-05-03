package ru.saidgadjiev.bibliographya.bussiness.moderation.operation;

import ru.saidgadjiev.bibliographya.dao.impl.BiographyModerationDao;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.column.ColumnSpec;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.AndCondition;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.Equals;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.literals.Param;
import ru.saidgadjiev.bibliographya.domain.Biography;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by said on 17.12.2018.
 */
public class ReleaseOperation {

    private final BiographyModerationDao biographyModerationDao;

    public ReleaseOperation(BiographyModerationDao biographyModerationDao) {
        this.biographyModerationDao = biographyModerationDao;
    }

    public Biography execute(Map<String, Object> args) throws SQLException {
        int biographyId = (int) args.get("biographyId");
        Integer moderatorId = (Integer) args.get("moderatorId");
        List<UpdateValue> values = new ArrayList<>();

        values.add(
                new UpdateValue<String>(
                        "moderator_id",
                        (preparedStatement, index) -> preparedStatement.setNull(index, Types.INTEGER)
                )
        );

        values.add(
                new UpdateValue<>(
                        "moderation_status",
                        (preparedStatement, index) -> preparedStatement.setInt(index, Biography.ModerationStatus.PENDING.getCode())
                )
        );

        return biographyModerationDao.update(values, new AndCondition() {{
            add(new Equals(new ColumnSpec(Biography.ID), new Param()));
            add(new Equals(new ColumnSpec(Biography.MODERATOR_ID), new Param()));
        }}, Arrays.asList(
                ((preparedStatement, index) -> preparedStatement.setInt(index, biographyId)),
                (preparedStatement, index) -> preparedStatement.setInt(index, moderatorId)
        ));
    }
}
