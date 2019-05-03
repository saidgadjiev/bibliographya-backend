package ru.saidgadjiev.bibliographya.bussiness.moderation.operation;

import ru.saidgadjiev.bibliographya.bussiness.common.BusinessOperation;
import ru.saidgadjiev.bibliographya.dao.impl.BiographyModerationDao;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.column.ColumnSpec;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.AndCondition;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.Equals;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.literals.Param;
import ru.saidgadjiev.bibliographya.domain.Biography;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by said on 17.03.2019.
 */
public class UserPendingOperation implements BusinessOperation<Biography> {

    private final BiographyModerationDao biographyModerationDao;

    public UserPendingOperation(BiographyModerationDao biographyModerationDao) {
        this.biographyModerationDao = biographyModerationDao;
    }

    @Override
    public Biography execute(Map<String, Object> args) throws SQLException {
        List<UpdateValue> values = new ArrayList<>();

        values.add(
                new UpdateValue<>(
                        Biography.MODERATION_STATUS,
                        (preparedStatement, index) -> preparedStatement.setInt(index, Biography.ModerationStatus.PENDING.getCode())
                )
        );

        int biographyId = (int) args.get("biographyId");
        int creatorId = (Integer) args.get("creatorId");

        return biographyModerationDao.update(values, new AndCondition() {{
            add(new Equals(new ColumnSpec(Biography.ID), new Param()));
            add(new Equals(new ColumnSpec(Biography.CREATOR_ID), new Param()));
        }}, Arrays.asList(
                ((preparedStatement, index) -> preparedStatement.setInt(index, biographyId)),
                (preparedStatement, index) -> preparedStatement.setInt(index, creatorId),
                (preparedStatement, index) -> preparedStatement.setInt(index, Biography.ModerationStatus.REJECTED.getCode())
        ));
    }
}
