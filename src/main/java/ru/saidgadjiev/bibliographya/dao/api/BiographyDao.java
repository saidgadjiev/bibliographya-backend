package ru.saidgadjiev.bibliographya.dao.api;

import org.springframework.data.domain.Sort;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.BiographyUpdateStatus;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by said on 08.01.2019.
 */
public interface BiographyDao {
    Biography save(Biography biography) throws SQLException;

    Biography save(Collection<UpdateValue> values) throws SQLException;

    List<Biography> getBiographiesList(int limit,
                                       long offset,
                                       Integer categoryId,
                                       Collection<FilterCriteria> biographyCriteria,
                                       Sort sort
    );

    long countOff();

    Biography getById(int id);

    BiographyUpdateStatus updateValues(Collection<UpdateValue> updateValues, Collection<FilterCriteria> criteria);

    int delete(int biographyId);

    List<Map<String, Object>> getFields(Collection<String> fields, Collection<FilterCriteria> criteria);
}
