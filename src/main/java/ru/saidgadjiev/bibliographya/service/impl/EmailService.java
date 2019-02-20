package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Created by said on 11.02.2019.
 */
@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    private Environment env;

    @Autowired
    public EmailService(JavaMailSender javaMailSender, Environment environment) {
        this.javaMailSender = javaMailSender;
        this.env = environment;
    }

    public void sendVerificationMessage(String email, int code) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setSubject("Подтверждение почты");
        mailMessage.setTo(email);
        mailMessage.setFrom(env.getProperty("support.email"));
        mailMessage.setText("Введите код: " + code + " для подтверждения почты!");

        javaMailSender.send(mailMessage);
    }
}
