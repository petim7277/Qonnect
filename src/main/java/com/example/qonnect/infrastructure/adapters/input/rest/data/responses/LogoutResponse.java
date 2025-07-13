package com.example.qonnect.infrastructure.adapters.input.rest.data.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class LogoutResponse {
    private String message;
    private LocalDateTime timestamp;
}
