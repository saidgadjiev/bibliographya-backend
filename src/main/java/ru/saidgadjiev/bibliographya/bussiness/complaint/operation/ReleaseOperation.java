package ru.saidgadjiev.bibliographya.bussiness.complaint.operation;

import ru.saidgadjiev.bibliographya.dao.impl.BiographyReportDao;

import java.util.Map;

/**
 * Created by said on 01.01.2019.
 */
@SuppressWarnings("PMD")
public class ReleaseOperation {

    private final BiographyReportDao reportDao;

    public ReleaseOperation(BiographyReportDao reportDao) {
        this.reportDao = reportDao;
    }

    public void execute(Map<String, Object> args) {
        /*List<UpdateValue> updateValues = new ArrayList<>();

        updateValues.add(
                new UpdateValue<>(
                        "consider_id",
                        null,
                        (preparedStatement, index, value) -> preparedStatement.setNull(index, Types.INTEGER)
                )
        );

        List<FilterCriteria> criteria = new ArrayList<>();
        Integer considerId = (Integer) args.get("considerId");

        criteria.add(
                new FilterCriteria<>(
                        "consider_id",
                        FilterOperation.EQ,
                        PreparedStatement::setInt,
                        considerId,
                        true
                )
        );

        reportDao.update(updateValues, criteria);*/
    }
}
