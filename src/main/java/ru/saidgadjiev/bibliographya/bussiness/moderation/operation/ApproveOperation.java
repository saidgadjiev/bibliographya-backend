package ru.saidgadjiev.bibliographya.bussiness.moderation.operation;

import ru.saidgadjiev.bibliographya.dao.impl.BiographyModerationDao;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.FilterOperation;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.domain.Biography;

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
                        Biography.ModerationStatus.APPROVED.getCode(),
                        true,
                        PreparedStatement::setInt
                )
        );
        values.add(
                new UpdateValue<>(
                        "publish_status",
                        Biography.PublishStatus.PUBLISHED.getCode(),
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

        int moderatorId = (Integer) args.get("moderatorId");

        criteria.add(
                new FilterCriteria<>(
                        "moderator_id",
                        FilterOperation.EQ,
                        PreparedStatement::setInt,
                        moderatorId,
                        true
                )
        );

        return biographyModerationDao.update(values, criteria);
    }
}
