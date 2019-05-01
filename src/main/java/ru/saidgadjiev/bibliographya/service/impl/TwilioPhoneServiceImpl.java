package ru.saidgadjiev.bibliographya.service.impl;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.configuration.BibliographyaConfiguration;
import ru.saidgadjiev.bibliographya.properties.TwilioProperties;
import ru.saidgadjiev.bibliographya.service.api.PhoneService;

@Service
@Profile(BibliographyaConfiguration.PROFILE_PROD)
public class TwilioPhoneServiceImpl implements PhoneService, InitializingBean {

    private TwilioProperties twilioProperties;

    @Autowired
    public TwilioPhoneServiceImpl(TwilioProperties twilioProperties) {
        this.twilioProperties = twilioProperties;
    }

    @Override
    public void sendSms(String number, String sms) {
        if (!number.startsWith("+")) {
            number = "+" + number;
        }

        Message.creator(
                new PhoneNumber(number),
                new PhoneNumber(twilioProperties.getNumber()),
                sms
        ).create();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Twilio.init(twilioProperties.getAccountSid(), twilioProperties.getAuthToken());
    }
}
