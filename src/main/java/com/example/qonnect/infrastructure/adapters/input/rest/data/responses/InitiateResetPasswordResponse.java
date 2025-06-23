package com.example.qonnect.infrastructure.adapters.input.rest.data.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Setter
@Getter
public class InitiateResetPasswordResponse {
    private String message;
    private LocalDateTime createdAt;

}