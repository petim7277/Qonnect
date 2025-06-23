package com.example.qonnect.application.input;

public interface ResetPasswordUseCase {


    void initiateReset(String email);
    void completeReset(String email, String otp, String newPassword);
}

