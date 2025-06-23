package com.example.qonnect.application.input;

public interface ChangePasswordUseCase {

    void changePassword(String email, String oldPassword, String newPassword);
}
