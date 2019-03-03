package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Created by said on 11.02.2019.
 */
@SuppressWarnings("PMD")
@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Autowired
    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendEmail(String email, String subject, String message) {
        System.out.println(message);
        /*SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setSubject(subject);
        mailMessage.setTo(email);
        mailMessage.setText(message);

        javaMailSender.send(mailMessage);*/
    }
}
