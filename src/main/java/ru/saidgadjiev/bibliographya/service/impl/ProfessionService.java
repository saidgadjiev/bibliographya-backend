package ru.saidgadjiev.bibliographya.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.dao.impl.GeneralDao;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.column.ColumnSpec;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.AndCondition;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.Like;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.function.Lower;
import ru.saidgadjiev.bibliographya.domain.Country;
import ru.saidgadjiev.bibliographya.domain.Profession;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProfessionService {

    private GeneralDao generalDao;

    public ProfessionService(GeneralDao generalDao) {
        this.generalDao = generalDao;
    }

    public List<Profession> getProfessions(String query) {
        AndCondition andCondition = null;

        if (StringUtils.isNotBlank(query)) {
            andCondition = new AndCondition() {{
                add(new Like(new Lower(new ColumnSpec(Profession.NAME)), "%" + query.toLowerCase() + "%"));
            }};
        }

        List<Map<String, Object>> fields = generalDao.getFields(Profession.TYPE, null, andCondition, null);

        return fields.stream().map(stringObjectMap -> {
            Profession country = new Profession();

            country.setId((Integer) stringObjectMap.get(Profession.ID));
            country.setName((String) stringObjectMap.get(Profession.NAME));

            return country;
        }).collect(Collectors.toList());
    }
}
