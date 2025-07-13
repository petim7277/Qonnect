package com.example.qonnect.application.input;

import com.example.qonnect.domain.models.enums.OtpType;

public interface ResendOtpUseCases {

    void resendOtp(String email, OtpType otpType);

}
