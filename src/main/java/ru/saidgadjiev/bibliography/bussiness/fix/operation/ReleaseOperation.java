package ru.saidgadjiev.bibliography.bussiness.fix.operation;

import ru.saidgadjiev.bibliography.dao.BiographyFixDao;
import ru.saidgadjiev.bibliography.data.FilterCriteria;
import ru.saidgadjiev.bibliography.data.FilterOperation;
import ru.saidgadjiev.bibliography.data.PreparedSetter;
import ru.saidgadjiev.bibliography.data.UpdateValue;
import ru.saidgadjiev.bibliography.domain.BiographyFix;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by said on 21.12.2018.
 */
public class ReleaseOperation {

    private final BiographyFixDao biographyFixDao;

    public ReleaseOperation(BiographyFixDao biographyFixDao) {
        this.biographyFixDao = biographyFixDao;
    }

    public BiographyFix execute(Map<String, Object> args) {
        List<UpdateValue> values = new ArrayList<>();

        values.add(
                new UpdateValue<>(
                        "status",
                        BiographyFix.FixStatus.PENDING.getCode(),
                        true,
                        PreparedStatement::setInt
                )
        );
        values.add(
                new UpdateValue<>(
                        "fixer_id",
                        null,
                        true,
                        (preparedStatement, index, value) -> preparedStatement.setNull(index, Types.INTEGER)
                )
        );

        List<FilterCriteria> criteria = new ArrayList<>();
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
