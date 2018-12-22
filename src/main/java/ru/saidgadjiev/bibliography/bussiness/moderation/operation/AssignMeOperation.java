package ru.saidgadjiev.bibliography.bussiness.moderation.operation;

import ru.saidgadjiev.bibliography.dao.BiographyModerationDao;
import ru.saidgadjiev.bibliography.data.FilterCriteria;
import ru.saidgadjiev.bibliography.data.FilterOperation;
import ru.saidgadjiev.bibliography.data.UpdateValue;
import ru.saidgadjiev.bibliography.domain.Biography;

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
        String moderatorName = (String) args.get("moderatorName");
        List<UpdateValue> values = new ArrayList<>();

        values.add(
                new UpdateValue<>(
                        "moderator_name",
                        moderatorName,
                        true,
                        PreparedStatement::setString
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
                        FilterOperation.IS_NULL,
                        null,
                        null,
                        false
                )
        );

        return biographyModerationDao.update(values, criteria);
    }
}
