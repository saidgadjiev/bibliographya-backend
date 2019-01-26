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
 * Created by said on 21.12.2018.
 */
public class AssignMeOperation {

    private final BiographyFixDao biographyFixDao;

    public AssignMeOperation(BiographyFixDao biographyFixDao) {
        this.biographyFixDao = biographyFixDao;
    }

    public BiographyFix execute(Map<String, Object> args) {
        List<UpdateValue> values = new ArrayList<>();
        int fixerId = (Integer) args.get("fixerId");

        values.add(
                new UpdateValue<>(
                        "fixer_id",
                        fixerId,
                        true,
                        PreparedStatement::setInt
                )
        );

        List<FilterCriteria> criteria = new ArrayList<>();

        criteria.add(
                new FilterCriteria<>(
                        "fixer_id",
                        FilterOperation.IS_NULL,
                        null,
                        null,
                        false
                )
        );

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

        return biographyFixDao.update(values, criteria);
    }
}
