package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.dao.impl.GeneralDao;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.column.ColumnSpec;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.AndCondition;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.Equals;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.literals.Param;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.domain.UserAccount;
import ru.saidgadjiev.bibliographya.model.GeneralSettings;
import ru.saidgadjiev.bibliographya.utils.SecureUtils;

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
                UserAccount.TABLE,
                Arrays.asList(UserAccount.EMAIL, UserAccount.PHONE),
                new AndCondition() {{
                    add(new Equals(new ColumnSpec(UserAccount.ID), new Param()));
                }},
                Collections.singletonList((preparedStatement, index) -> preparedStatement.setInt(index, loggedInUser.getUserAccount().getId()))
        );
        Map<String, Object> values = fieldsValues.get(0);

        generalSettings.setEmail(SecureUtils.secureEmail((String) values.get(UserAccount.EMAIL)));
        generalSettings.setPhone(SecureUtils.securePhone((String) values.get(UserAccount.PHONE)));

        return generalSettings;
    }
}
