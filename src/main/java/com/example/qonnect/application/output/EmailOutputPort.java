package com.example.qonnect.application.output;

public interface EmailOutputPort {
    void sendEmail(String to, String subject, String body);
}
