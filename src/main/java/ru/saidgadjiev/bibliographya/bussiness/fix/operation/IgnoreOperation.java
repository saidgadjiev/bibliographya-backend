package ru.saidgadjiev.bibliographya.bussiness.fix.operation;

import ru.saidgadjiev.bibliographya.dao.impl.BiographyFixDao;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.FilterOperation;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.domain.BiographyFix;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by said on 20.01.2019.
 */
public class IgnoreOperation {

    private final BiographyFixDao biographyFixDao;

    public IgnoreOperation(BiographyFixDao biographyFixDao) {
        this.biographyFixDao = biographyFixDao;
    }

    public BiographyFix execute(Map<String, Object> args) {
        List<UpdateValue> updateValues = new ArrayList<>();
        String fixInfo = (String) args.get("fixInfo");

        updateValues.add(
                new UpdateValue<>(
                        "fix_info",
                        fixInfo,
                        true,
                        PreparedStatement::setString
                )
        );

        updateValues.add(
                new UpdateValue<>(
                        "status",
                        BiographyFix.FixStatus.IGNORED.getCode(),
                        true,
                        PreparedStatement::setInt

                )
        );

        List<FilterCriteria> criteria = new ArrayList<>();
        int id = (int) args.get("fixId");

        criteria.add(
                new FilterCriteria<>(
                        "id",
                        FilterOperation.EQ,
                        PreparedStatement::setInt,
                        id,
                        true
                )
        );
        int fixerId = (Integer) args.get("fixerId");

        criteria.add(
                new FilterCriteria<>(
                        "fixer_id",
                        FilterOperation.EQ,
                        PreparedStatement::setInt,
                        fixerId,
                        true
                )
        );

        return biographyFixDao.update(updateValues, criteria);
    }
}
