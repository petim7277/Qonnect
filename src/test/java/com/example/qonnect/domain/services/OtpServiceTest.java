package com.example.qonnect.domain.services;

import com.example.qonnect.application.output.EmailOutputPort;
import com.example.qonnect.application.output.OtpOutputPort;
import com.example.qonnect.domain.exceptions.OtpException;
import com.example.qonnect.domain.models.Otp;
import com.example.qonnect.domain.models.OtpType;
import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class OtpServiceTest {

    @Mock
    private OtpOutputPort otpOutputPort;

    @Mock
    private EmailOutputPort emailOutputPort;

    private OtpService otpService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        otpService = new OtpService(otpOutputPort, emailOutputPort);
        otpService.setResetPasswordExpiry(10L);
        otpService.setVerificationExpiry(10L);
    }

    @Test
    void testCreateOtpSuccess() {
        String name = "Praise";
        String email = "praise@example.com";
        OtpType otpType = OtpType.VERIFICATION;

        Otp otp = otpService.createOtp(name, email, otpType);

        assertNotNull(otp);
        assertEquals(email, otp.getEmail());
        assertEquals(otpType, otp.getOtpType());
        assertFalse(otp.isUsed());

        verify(otpOutputPort, times(1)).saveOtp(any());
        verify(emailOutputPort, times(1)).sendEmail(eq(email), anyString(), contains("OTP"));
    }


@Test
    void testValidateOtp_Success() {
        String testEmail = "test@example.com";
        String testCode = "123456";
        Otp mockOtp = Otp.builder()
                .email(testEmail)
                .otp(testCode)
                .used(false)
                .expiryTime(LocalDateTime.now().plusMinutes(10))
                .build();

        when(otpOutputPort.findByEmailAndOtp(testEmail, testCode)).thenReturn(mockOtp);

        assertDoesNotThrow(() -> otpService.validateOtp(testEmail, testCode));
        verify(otpOutputPort).saveOtp(mockOtp);
    }

    @Test
    void testValidateOtp_AlreadyUsed() {
        Otp mockOtp = Otp.builder().used(true).expiryTime(LocalDateTime.now().plusMinutes(5)).build();
        when(otpOutputPort.findByEmailAndOtp("a@b.com", "123456")).thenReturn(mockOtp);

        OtpException ex = assertThrows(OtpException.class, () -> otpService.validateOtp("a@b.com", "123456"));
        assertEquals(ErrorMessages.OTP_ALREADY_USED, ex.getMessage());
    }

    @Test
    void testValidateOtp_Expired() {
        Otp mockOtp = Otp.builder().used(false).expiryTime(LocalDateTime.now().minusMinutes(1)).build();
        when(otpOutputPort.findByEmailAndOtp("a@b.com", "123456")).thenReturn(mockOtp);

        OtpException ex = assertThrows(OtpException.class, () -> otpService.validateOtp("a@b.com", "123456"));
        assertEquals(ErrorMessages.OTP_ALREADY_EXPIRED, ex.getMessage());
    }
}