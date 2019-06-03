package ru.saidgadjiev.bibliographya.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.dao.impl.BiographyProfessionDao;
import ru.saidgadjiev.bibliographya.dao.impl.GeneralDao;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.column.ColumnSpec;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.AndCondition;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.Like;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.function.Lower;
import ru.saidgadjiev.bibliographya.domain.Profession;
import ru.saidgadjiev.bibliographya.model.BiographyProfession;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProfessionService {

    private GeneralDao generalDao;

    private BiographyProfessionDao biographyProfessionDao;

    @Autowired
    public ProfessionService(GeneralDao generalDao, BiographyProfessionDao biographyProfessionDao) {
        this.generalDao = generalDao;
        this.biographyProfessionDao = biographyProfessionDao;
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
        }).sorted().collect(Collectors.toList());
    }

    public Map<Integer, BiographyProfession> getBiographiesProfessions(Collection<Integer> biographiesIds) {
        return biographyProfessionDao.getBiographiesProfessions(biographiesIds);
    }

    public BiographyProfession getBiographyProfessions(Integer biographyId) {
        return biographyProfessionDao.getBiographiesProfessions(Collections.singletonList(biographyId)).get(biographyId);
    }

    public void addProfessionsToBiography(List<Integer> professionsIds, Integer biographyId) {
        biographyProfessionDao.addProfessions(professionsIds, biographyId);
    }

    public void deleteProfessionsFromBiography(List<Integer> professionsIds, Integer biographyId) {
        biographyProfessionDao.deleteProfessions(professionsIds, biographyId);
    }

}
