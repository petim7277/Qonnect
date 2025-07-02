package com.example.qonnect.infrastructure.adapters.output.persistence.adapters;

import com.example.qonnect.domain.exceptions.OtpNotFoundException;
import com.example.qonnect.domain.models.Otp;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.OtpEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.mappers.OtpPersistenceMapper;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.OtpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
class OtpPersistenceAdapterTest {

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private OtpPersistenceMapper otpPersistenceMapper;

    @Autowired
    private OtpPersistenceAdapter otpPersistenceAdapter;

    private Otp savedOtp;

    @BeforeEach
    void setup() {
        OtpEntity entity = OtpEntity.builder()
                .email("user@example.com")
                .otp("123456")
                .used(false)
                .createdAt(LocalDateTime.now())
                .expiryTime(LocalDateTime.now().plusMinutes(10))
                .build();

        otpRepository.save(entity);
        savedOtp = otpPersistenceMapper.toOtp(entity);
    }

    @Test
    void findByEmailAndOtp_shouldReturnOtp() {
        Otp result = otpPersistenceAdapter.findByEmailAndOtp("user@example.com", "123456");
        assertNotNull(result);
        assertEquals("123456", result.getOtp());
    }

    @Test
    void saveOtp_shouldPersistSuccessfully() {
        Otp newOtp = Otp.builder()
                .email("new@example.com")
                .otp("999999")
                .createdAt(LocalDateTime.now())
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();

        Otp saved = otpPersistenceAdapter.saveOtp(newOtp);

        assertNotNull(saved.getId());
        assertEquals("new@example.com", saved.getEmail());
    }

    @Test
    void findByEmailAndOtp_shouldThrowException_whenNotFound() {
        assertThrows(OtpNotFoundException.class, () ->
                otpPersistenceAdapter.findByEmailAndOtp("invalid@example.com", "000000"));
    }
}
