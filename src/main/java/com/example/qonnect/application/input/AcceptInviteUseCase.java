package com.example.qonnect.application.input;

public interface AcceptInviteUseCase {

    void completeInvitation(String inviteToken, String firstName, String lastName, String password);
    void verifyOtpAndActivate(String inviteToken, String otp);
}
