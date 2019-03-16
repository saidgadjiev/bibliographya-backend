package ru.saidgadjiev.bibliographya.service.api;

import javax.mail.MessagingException;

/**
 * Created by said on 16.03.2019.
 */
public interface EmailService {
    void sendEmail(String email, String subject, String message) throws MessagingException;
}
