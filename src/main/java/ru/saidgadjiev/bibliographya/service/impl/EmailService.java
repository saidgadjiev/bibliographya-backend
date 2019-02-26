package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.PropertyPlaceholderHelper;

import javax.mail.internet.MimeMessage;

/**
 * Created by said on 11.02.2019.
 */
@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    private MessageSource messageSource;

    @Autowired
    public EmailService(JavaMailSender javaMailSender, MessageSource messageSource) {
        this.javaMailSender = javaMailSender;
        this.messageSource = messageSource;
    }

    public void sendEmail(String email, String message, String subject, int code) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);

        mailMessage.setSubject("Подтверждение почты");
        mailMessage.setTo(email);
        mailMessage.setText(message);

        messageSource.getMessage()

        javaMailSender.send(mailMessage);
    }
}
