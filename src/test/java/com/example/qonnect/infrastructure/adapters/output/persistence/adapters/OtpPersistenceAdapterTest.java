package com.example.qonnect.infrastructure.adapters.output.persistence.adapters;

import com.example.qonnect.domain.exceptions.OtpNotFoundException;
import com.example.qonnect.domain.models.Otp;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.OtpEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.mappers.OtpPersistenceMapper;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.OtpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OtpPersistenceAdapterTest {

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private OtpPersistenceMapper otpPersistenceMapper;

    @InjectMocks
    private OtpPersistenceAdapter otpPersistenceAdapter;

    private final String email = "user@example.com";
    private final String otpCode = "123456";

    private Otp domainOtp;
    private OtpEntity entityOtp;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        domainOtp = Otp.builder()
                .id(1L)
                .email(email)
                .otp(otpCode)
                .used(false)
                .createdAt(LocalDateTime.now())
                .expiryTime(LocalDateTime.now().plusMinutes(10))
                .build();

        entityOtp = OtpEntity.builder()
                .id(1L)
                .email(email)
                .otp(otpCode)
                .used(false)
                .createdAt(domainOtp.getCreatedAt())
                .expiryTime(domainOtp.getExpiryTime())
                .build();
    }

    @Test
    void saveOtp_shouldSaveAndReturnMappedDomainObject() {
        when(otpPersistenceMapper.toOtpEntity(domainOtp)).thenReturn(entityOtp);
        when(otpRepository.save(entityOtp)).thenReturn(entityOtp);
        when(otpPersistenceMapper.toOtp(entityOtp)).thenReturn(domainOtp);

        Otp result = otpPersistenceAdapter.saveOtp(domainOtp);

        assertNotNull(result);
        assertEquals(domainOtp.getEmail(), result.getEmail());
        verify(otpRepository).save(entityOtp);
        verify(otpPersistenceMapper).toOtpEntity(domainOtp);
        verify(otpPersistenceMapper).toOtp(entityOtp);
    }

    @Test
    void findByEmailAndOtp_shouldReturnMappedDomainObject_whenFound() {
        when(otpRepository.findByEmailAndOtp(email, otpCode)).thenReturn(Optional.of(entityOtp));
        when(otpPersistenceMapper.toOtp(entityOtp)).thenReturn(domainOtp);

        Otp result = otpPersistenceAdapter.findByEmailAndOtp(email, otpCode);

        assertNotNull(result);
        assertEquals(otpCode, result.getOtp());
        verify(otpRepository).findByEmailAndOtp(email, otpCode);
        verify(otpPersistenceMapper).toOtp(entityOtp);
    }

    @Test
    void findByEmailAndOtp_shouldThrowException_whenNotFound() {
        when(otpRepository.findByEmailAndOtp(email, otpCode)).thenReturn(Optional.empty());

        OtpNotFoundException exception = assertThrows(OtpNotFoundException.class, () ->
                otpPersistenceAdapter.findByEmailAndOtp(email, otpCode));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(otpRepository).findByEmailAndOtp(email, otpCode);
        verifyNoInteractions(otpPersistenceMapper);
    }
}
