package com.example.qonnect.infrastructure.adapters.output.persistence.adapters;

import com.example.qonnect.application.output.OtpOutputPort;
import com.example.qonnect.domain.exceptions.OtpNotFoundException;
import com.example.qonnect.domain.models.Otp;
import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.OtpEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.mappers.OtpPersistenceMapper;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class OtpPersistenceAdapter implements OtpOutputPort {

    private final OtpRepository otpRepository;
    private final OtpPersistenceMapper otpPersistenceMapper;

    @Override
    public Otp saveOtp(Otp otp) {

        log.info("Saving otp: {}", otp);

        OtpEntity entity = otpPersistenceMapper.toOtpEntity(otp);
        log.info("Mapped to entity: {}", entity);

        entity = otpRepository.save(entity);
        log.info("Saved entity: {}", entity);

        Otp savedOtp = otpPersistenceMapper.toOtp(entity);
        log.info("Mapped back to domain user: {}", savedOtp);

        return savedOtp;

}

    @Override
    public Otp findByEmailAndOtp(String email, String otp) {
        OtpEntity entity = otpRepository.findByEmailAndOtp(email,otp).orElseThrow(()->new OtpNotFoundException(ErrorMessages.INVALID_OTP, HttpStatus.BAD_REQUEST));
        return otpPersistenceMapper.toOtp(entity);
    }


}


