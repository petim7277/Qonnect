package com.example.qonnect.application.input;

import com.example.qonnect.domain.models.Otp;
import com.example.qonnect.domain.models.enums.OtpType;

public interface CreateOtpUseCase {

    Otp createOtp(String name, String email, OtpType otpType);
}
