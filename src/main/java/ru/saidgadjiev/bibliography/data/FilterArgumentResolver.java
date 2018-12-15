package ru.saidgadjiev.bibliography.data;

import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Created by said on 24.11.2018.
 */
@Service
public class FilterArgumentResolver {

    public<R> FilterCriteria resolve(String propertyName,
                                  Function<Object, R> converter,
                                  PreparedSetter<R> valueSetter,
                                  String filter) {
        FilterCriteria criteria = new FilterCriteria();

        criteria.setValueSetter((PreparedSetter<Object>) valueSetter);
        criteria.setPropertyName(propertyName);

        String split[] = filter.split(":");
        FilterOperation operation = FilterOperation.from(split[0]);

        criteria.setFilterOperation(operation);

        switch (operation) {
            case EQ:
                String value = split[1];

                criteria.setFilterValue(converter.apply(value));

                return criteria;
            case IS_NULL:
                criteria.setNeedPreparedSet(false);

                return criteria;
        }

        return null;
    }
}
