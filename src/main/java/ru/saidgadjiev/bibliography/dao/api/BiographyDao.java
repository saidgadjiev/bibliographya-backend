package ru.saidgadjiev.bibliography.dao.api;

import org.springframework.data.domain.Sort;
import ru.saidgadjiev.bibliography.data.FilterCriteria;
import ru.saidgadjiev.bibliography.data.UpdateValue;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.BiographyUpdateStatus;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * Created by said on 08.01.2019.
 */
public interface BiographyDao {
    Biography save(Biography biography) throws SQLException;

    Biography getBiography(Collection<FilterCriteria> biographyCriteria);

    List<Biography> getBiographiesList(int limit,
                                       long offset,
                                       String categoryName,
                                       Collection<FilterCriteria> biographyCriteria,
                                       Sort sort
    );

    long countOff();

    Biography getById(int id);

    BiographyUpdateStatus update(Biography biography) throws SQLException;

    int updateValues(Collection<UpdateValue> updateValues, Collection<FilterCriteria> criteria);

    int delete(int biographyId);
}
