package ru.saidgadjiev.bibliography.bussiness.fix.operation;

import ru.saidgadjiev.bibliography.dao.BiographyFixDao;
import ru.saidgadjiev.bibliography.data.FilterCriteria;
import ru.saidgadjiev.bibliography.data.FilterOperation;
import ru.saidgadjiev.bibliography.data.PreparedSetter;
import ru.saidgadjiev.bibliography.data.UpdateValue;
import ru.saidgadjiev.bibliography.domain.BiographyFix;

import java.sql.PreparedStatement;
import java.sql.SQLException;
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
        String fixerName = (String) args.get("fixerName");

        values.add(
                new UpdateValue<>(
                        "fixer_name",
                        fixerName,
                        true,
                        PreparedStatement::setString
                )
        );

        List<FilterCriteria> criteria = new ArrayList<>();

        criteria.add(
                new FilterCriteria<String>(
                        "fixer_name",
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
