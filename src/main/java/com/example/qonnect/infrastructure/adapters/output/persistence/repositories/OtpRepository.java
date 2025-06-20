package com.example.qonnect.infrastructure.adapters.output.persistence.repositories;

import com.example.qonnect.infrastructure.adapters.output.persistence.entities.OtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<OtpEntity, Long> {

    Optional<OtpEntity> findByEmailAndOtp(String email, String otp);
}
