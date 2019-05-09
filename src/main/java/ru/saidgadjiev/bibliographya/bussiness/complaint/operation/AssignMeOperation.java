package ru.saidgadjiev.bibliographya.bussiness.complaint.operation;

import ru.saidgadjiev.bibliographya.dao.impl.BiographyReportDao;

import java.util.Map;

/**
 * Created by said on 01.01.2019.
 */
@SuppressWarnings("PMD")
public class AssignMeOperation {

    private final BiographyReportDao reportDao;

    public AssignMeOperation(BiographyReportDao reportDao) {
        this.reportDao = reportDao;
    }

    public void execute(Map<String, Object> args) {
        /*List<UpdateValue> updateValues = new ArrayList<>();
        Integer considerId = (Integer) args.get("considerId");

        updateValues.add(
                new UpdateValue<>(
                        "consider_id",
                        considerId,
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

        reportDao.update(updateValues, criteria);*/
    }
}
