package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.configuration.BibliographyaConfiguration;
import ru.saidgadjiev.bibliographya.service.api.PhoneService;

@Service
@Profile({BibliographyaConfiguration.PROFILE_DEV, BibliographyaConfiguration.PROFILE_TEST})
public class DummyPhoneService implements PhoneService {

    @Override
    public void sendSms(String number, String sms) {
        System.out.println("Send sms to " + number);
        System.out.println("Content: " + sms);
    }
}
