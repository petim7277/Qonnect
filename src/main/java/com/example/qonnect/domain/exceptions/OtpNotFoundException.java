package com.example.qonnect.domain.exceptions;

import org.springframework.http.HttpStatus;

public class OtpNotFoundException extends QonnectException{
    public OtpNotFoundException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
