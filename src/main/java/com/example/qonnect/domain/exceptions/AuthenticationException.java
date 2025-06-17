package com.example.qonnect.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class AuthenticationException extends QonnectException {
    public AuthenticationException(String message, HttpStatus status) {
        super(message, status);
    }
}
