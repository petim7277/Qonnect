package com.example.qonnect.infrastructure.adapters.output.persistence.entities;


import com.example.qonnect.domain.models.OtpType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "otps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String otp;

    private String email;

    @Enumerated(EnumType.STRING)
    private OtpType otpType;

    private boolean used;

    private LocalDateTime expiryTime;

    private LocalDateTime createdAt;
}
