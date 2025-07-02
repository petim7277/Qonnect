package com.example.qonnect.infrastructure.adapters.output.persistence.mappers;

import com.example.qonnect.domain.models.Otp;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.OtpEntity;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)

public interface OtpPersistenceMapper {
    Otp toOtp(OtpEntity otpEntity);

    OtpEntity toOtpEntity(Otp otp);
}
