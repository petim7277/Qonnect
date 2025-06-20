package com.example.qonnect.domain.exceptions;

import org.springframework.http.HttpStatus;

public class OtpException extends QonnectException{
    public OtpException(String message, HttpStatus status) {
        super(message, status);
    }
}
