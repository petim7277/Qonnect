package com.example.qonnect.domain.exceptions;

import org.springframework.http.HttpStatus;

public class IdentityManagementException extends QonnectException{
    public IdentityManagementException(String message, HttpStatus status) {
        super(message, status);
    }
}
