package com.example.qonnect.domain.exceptions;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends QonnectException{
    public UserNotFoundException(String message, HttpStatus status) {
        super(message, status);
    }
}
