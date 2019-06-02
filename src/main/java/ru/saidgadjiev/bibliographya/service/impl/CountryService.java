package ru.saidgadjiev.bibliographya.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.dao.impl.GeneralDao;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.column.ColumnSpec;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.AndCondition;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.Like;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.function.Lower;
import ru.saidgadjiev.bibliographya.domain.Country;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CountryService {

    private GeneralDao generalDao;

    public CountryService(GeneralDao generalDao) {
        this.generalDao = generalDao;
    }

    public List<Country> getCountries(String query) {
        AndCondition andCondition = null;

        if (StringUtils.isNotBlank(query)) {
            andCondition = new AndCondition() {{
                add(new Like(new Lower(new ColumnSpec(Country.NAME)), "%" + query.toLowerCase() + "%"));
            }};
        }

        List<Map<String, Object>> fields = generalDao.getFields(Country.TYPE, null, andCondition, null);

        return fields.stream().map(stringObjectMap -> {
            Country country = new Country();

            country.setId((Integer) stringObjectMap.get(Country.ID));
            country.setName((String) stringObjectMap.get(Country.NAME));

            return country;
        }).collect(Collectors.toList());
    }
}
