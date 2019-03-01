package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;


class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    void sendVerificationMessage() {
        emailService.sendEmail("g.said.alievich@mail.ru", 1234);
    }
}