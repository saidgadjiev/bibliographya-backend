package ru.saidgadjiev.bibliographya.bussiness.moderation.operation;

import ru.saidgadjiev.bibliographya.bussiness.common.BusinessOperation;
import ru.saidgadjiev.bibliographya.dao.impl.BiographyModerationDao;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.FilterOperation;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.domain.Biography;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
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
                        Biography.ModerationStatus.PENDING.getCode(),
                        PreparedStatement::setInt
                )
        );

        List<FilterCriteria> criteria = new ArrayList<>();
        int biographyId = (int) args.get("biographyId");

        criteria.add(
                new FilterCriteria<>(
                        Biography.ID,
                        FilterOperation.EQ,
                        PreparedStatement::setInt,
                        biographyId,
                        true
                )
        );

        int creatorId = (Integer) args.get("creatorId");

        criteria.add(
                new FilterCriteria<>(
                        Biography.CREATOR_ID,
                        FilterOperation.EQ,
                        PreparedStatement::setInt,
                        creatorId,
                        true
                )
        );

        criteria.add(
                new FilterCriteria.Builder<Integer>()
                        .propertyName(Biography.MODERATION_STATUS)
                        .filterOperation(FilterOperation.EQ)
                        .filterValue(Biography.ModerationStatus.REJECTED.getCode())
                        .needPreparedSet(true)
                        .valueSetter(PreparedStatement::setInt)
                        .build()
        );

        return biographyModerationDao.update(values, criteria);
    }
}
