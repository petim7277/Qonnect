package com.example.qonnect.application.output;

import com.example.qonnect.domain.models.Otp;

public interface OtpOutputPort {

    Otp saveOtp(Otp otp);

    Otp findByEmailAndOtp(String email, String otp);
}
