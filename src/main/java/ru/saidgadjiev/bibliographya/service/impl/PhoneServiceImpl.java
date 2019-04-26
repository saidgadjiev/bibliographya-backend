package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.configuration.BibliographyaConfiguration;
import ru.saidgadjiev.bibliographya.service.api.PhoneService;

@Service
@Profile(BibliographyaConfiguration.PROFILE_PROD)
public class PhoneServiceImpl implements PhoneService {

    @Override
    public void sendSms(String number, String sms) {

    }
}
