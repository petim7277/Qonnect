package com.example.qonnect.infrastructure.adapters.input.rest.data.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CompleteResetPasswordResponse {
    private String message;
    private LocalDateTime time;
}
