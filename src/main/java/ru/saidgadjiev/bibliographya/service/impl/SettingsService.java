package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.dao.impl.GeneralDao;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.FilterOperation;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.model.GeneralSettings;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class SettingsService {

    private GeneralDao generalDao;

    private SecurityService securityService;

    @Autowired
    public SettingsService(GeneralDao generalDao, SecurityService securityService) {
        this.generalDao = generalDao;
        this.securityService = securityService;
    }

    public GeneralSettings getEmail() {
        return getGeneralSettings();
    }

    public GeneralSettings getGeneralSettings() {
        GeneralSettings generalSettings = new GeneralSettings();
        User loggedInUser = (User) securityService.findLoggedInUser();

        List<Map<String, Object>> fieldsValues = generalDao.getFields(
                User.TABLE,
                Arrays.asList(User.EMAIL, User.EMAIL_VERIFIED),
                Collections.singletonList(
                        new FilterCriteria.Builder<Integer>()
                                .propertyName(User.ID)
                                .filterOperation(FilterOperation.EQ)
                                .filterValue(loggedInUser.getId())
                                .needPreparedSet(true)
                                .valueSetter(PreparedStatement::setInt)
                                .build()
                )
        );
        Map<String, Object> values = fieldsValues.get(0);

        generalSettings.setEmail((String) values.get(User.EMAIL));
        generalSettings.setEmailVerified((Boolean) values.get(User.EMAIL_VERIFIED));

        return generalSettings;
    }
}
