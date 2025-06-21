package com.example.qonnect.infrastructure.adapters.input.rest.data.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
@Setter
@Getter
@AllArgsConstructor
public class ErrorResponse {
    private int statusCode;
    private String message;
    private Instant timeStamp;
}
