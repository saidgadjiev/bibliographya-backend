package ru.saidgadjiev.bibliographya.bussiness.moderation.operation;

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
                        moderatorId,
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
                        "moderator_id",
                        FilterOperation.IS_NULL,
                        null,
                        null,
                        false
                )
        );

        return biographyModerationDao.update(values, criteria);
    }
}
