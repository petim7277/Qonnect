package com.example.qonnect.domain.services;

import com.example.qonnect.application.input.CreateOtpUseCase;
import com.example.qonnect.application.input.ResendOtpUseCase;
import com.example.qonnect.application.input.ValidateOtpUseCase;
import com.example.qonnect.application.output.EmailOutputPort;
import com.example.qonnect.application.output.OtpOutputPort;
import com.example.qonnect.domain.exceptions.OtpException;
import com.example.qonnect.domain.models.Otp;
import com.example.qonnect.domain.models.enums.OtpType;
import com.example.qonnect.domain.template.EmailTemplate;
import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.example.qonnect.domain.validators.InputValidator.validateInput;

@Service
@RequiredArgsConstructor
public class OtpService implements CreateOtpUseCase, ValidateOtpUseCase, ResendOtpUseCase {

    private final OtpOutputPort otpOutputPort;
    private final EmailOutputPort emailOutputPort;

    @Setter
    @Value("${otp.expiry.reset-password}")
    private Long resetPasswordExpiry;

    @Setter
    @Value("${otp.expiry.verify-account}")
    private Long verificationExpiry;

    @Override
    public Otp createOtp(String name, String email, OtpType otpType) {
        String otp = generateOtp();
        LocalDateTime expiryTime = calculateExpiryTime(otpType);

        Otp otpObj = Otp.builder()
                .otp(otp)
                .email(email)
                .otpType(otpType)
                .used(false)
                .createdAt(LocalDateTime.now())
                .expiryTime(expiryTime)
                .build();

        otpOutputPort.saveOtp(otpObj);

        String body = switch (otpType) {
            case VERIFICATION -> EmailTemplate.otpTemplate(name, otp);
            case RESET_PASSWORD -> EmailTemplate.resetPasswordTemplate(name, otp);
        };

        String subject = switch (otpType) {
            case VERIFICATION -> "Your OTP for Registration";
            case RESET_PASSWORD -> "Your OTP to Reset Password";
        };

        emailOutputPort.sendEmail(email, subject, body);

        return otpObj;
    }

    @Override
    public Otp resendOtp(String name, String email, OtpType otpType) {
        return createOtp(name, email, otpType);
    }

    @Override
    public void validateOtp(String email, String otpInput) {
        validateInput(email);
        validateInput(otpInput);

        Otp otp = otpOutputPort.findByEmailAndOtp(email, otpInput);

        if (otp.isUsed()) {
            throw new OtpException(ErrorMessages.OTP_ALREADY_USED, HttpStatus.BAD_REQUEST);
        }

        if (otp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new OtpException(ErrorMessages.OTP_ALREADY_EXPIRED, HttpStatus.BAD_REQUEST);
        }

        otp.setUsed(true);
        otpOutputPort.saveOtp(otp);
    }

    private String generateOtp() {
        return RandomStringUtils.randomNumeric(6);
    }

    private LocalDateTime calculateExpiryTime(OtpType otpType) {
        Long minutes = switch (otpType) {
            case RESET_PASSWORD -> resetPasswordExpiry;
            case VERIFICATION -> verificationExpiry;
        };
        return LocalDateTime.now().plusMinutes(minutes);
    }
}
