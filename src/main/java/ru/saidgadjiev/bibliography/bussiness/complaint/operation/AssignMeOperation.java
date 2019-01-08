package ru.saidgadjiev.bibliography.bussiness.complaint.operation;

import ru.saidgadjiev.bibliography.dao.impl.BiographyReportDao;
import ru.saidgadjiev.bibliography.data.FilterCriteria;
import ru.saidgadjiev.bibliography.data.FilterOperation;
import ru.saidgadjiev.bibliography.data.UpdateValue;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by said on 01.01.2019.
 */
public class AssignMeOperation {

    private final BiographyReportDao reportDao;

    public AssignMeOperation(BiographyReportDao reportDao) {
        this.reportDao = reportDao;
    }

    public void execute(Map<String, Object> args) {
        List<UpdateValue> updateValues = new ArrayList<>();
        Integer considerId = (Integer) args.get("considerId");

        updateValues.add(
                new UpdateValue<>(
                        "consider_id",
                        considerId,
                        true,
                        PreparedStatement::setInt
                )
        );

        List<FilterCriteria> criteria = new ArrayList<>();

        criteria.add(
                new FilterCriteria<Integer>(
                        "consider_id",
                        FilterOperation.IS_NULL,
                        null,
                        null,
                        false
                )
        );

        reportDao.update(updateValues, criteria);
    }
}
