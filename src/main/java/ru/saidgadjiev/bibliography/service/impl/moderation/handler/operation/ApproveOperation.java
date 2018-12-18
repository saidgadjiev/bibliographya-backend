package ru.saidgadjiev.bibliography.service.impl.moderation.handler.operation;

import ru.saidgadjiev.bibliography.dao.BiographyModerationDao;
import ru.saidgadjiev.bibliography.data.FilterCriteria;
import ru.saidgadjiev.bibliography.data.FilterOperation;
import ru.saidgadjiev.bibliography.data.PreparedSetter;
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
public class ApproveOperation {

    private final BiographyModerationDao biographyModerationDao;

    public ApproveOperation(BiographyModerationDao biographyModerationDao) {
        this.biographyModerationDao = biographyModerationDao;
    }

    public Biography execute(Map<String, Object> args) throws SQLException {
        List<UpdateValue> values = new ArrayList<>();

        values.add(
                new UpdateValue<>(
                        "moderation_status",
                        ModerationStatus.APPROVED.getCode(),
                        true,
                        PreparedStatement::setInt
                )
        );

        values.add(
                new UpdateValue<>(
                        "moderation_info",
                        null,
                        true,
                        (preparedStatement, index, value) -> preparedStatement.setNull(index, Types.VARCHAR)
                )
        );

        List<FilterCriteria> criteria = new ArrayList<>();

        int biographyId = (int) args.get("biographyId");

        criteria.add(
                new FilterCriteria<>(
                        "id",
                        FilterOperation.EQ,
                        PreparedStatement::setInt,
                        biographyId,
                        true
                )
        );

        String moderatorName = (String) args.get("moderatorName");

        criteria.add(
                new FilterCriteria<String>(
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
