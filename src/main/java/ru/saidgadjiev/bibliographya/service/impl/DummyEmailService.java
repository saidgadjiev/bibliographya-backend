package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.configuration.BibliographyaConfiguration;
import ru.saidgadjiev.bibliographya.service.api.EmailService;

import javax.mail.MessagingException;

/**
 * Created by said on 16.03.2019.
 */
@Profile({BibliographyaConfiguration.PROFILE_DEV, BibliographyaConfiguration.PROFILE_TEST })
@Service
public class DummyEmailService implements EmailService {

    @Override
    public void sendEmail(String email, String subject, String message) throws MessagingException {
        System.out.println("Send message to: " + email);
        System.out.println("Message: "+ message);
    }
}
