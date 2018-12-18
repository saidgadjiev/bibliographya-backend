package ru.saidgadjiev.bibliography.service.impl.moderation.handler.operation;

import ru.saidgadjiev.bibliography.dao.BiographyModerationDao;
import ru.saidgadjiev.bibliography.data.FilterCriteria;
import ru.saidgadjiev.bibliography.data.FilterOperation;
import ru.saidgadjiev.bibliography.data.UpdateValue;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.model.ModerationStatus;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
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
        String moderatorName = (String) args.get("moderatorName");
        List<UpdateValue> values = new ArrayList<>();

        values.add(
                new UpdateValue<String>(
                        "moderator_name",
                        null,
                        true,
                        (preparedStatement, index, value) -> preparedStatement.setNull(index, Types.VARCHAR)
                )
        );

        values.add(
                new UpdateValue<>(
                        "moderation_status",
                        ModerationStatus.PENDING.getCode(),
                        true,
                        PreparedStatement::setInt
                )
        );

        List<FilterCriteria> criteria = new ArrayList<>();

        criteria.add(
                new FilterCriteria<>(
                        "id",
                        FilterOperation.EQ,
                        PreparedStatement::setInt,
                        biographyId,
                        true
                )
        );

        criteria.add(
                new FilterCriteria<>(
                        "moderator_name",
                        FilterOperation.EQ,
                        PreparedStatement::setString,
                        moderatorName,
                        true
                )
        );

        return biographyModerationDao.update(values, criteria);
    }
}
