package com.example.qonnect.domain.models;


import com.example.qonnect.domain.models.enums.OtpType;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Otp {

        private Long id;

        private String otp;

        private String email;

        private OtpType otpType;

        private boolean used;

        private LocalDateTime expiryTime;

        private LocalDateTime createdAt;
}
