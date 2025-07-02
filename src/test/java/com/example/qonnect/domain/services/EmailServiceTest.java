package com.example.qonnect.domain.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.*;

class EmailServiceTest {

    private JavaMailSender mailSender;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
            mailSender = mock(JavaMailSender.class);
            emailService = new EmailService(mailSender);
        }



    @Test
    void testSendEmail() {
        String to = "praiseoyewole560@gmail.com";
        String subject = "Test Subject";
        String body = "This is a test email.";

        emailService.sendEmail(to, subject, body);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
