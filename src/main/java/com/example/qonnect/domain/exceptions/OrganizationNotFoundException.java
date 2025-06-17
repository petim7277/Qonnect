package com.example.qonnect.domain.exceptions;

import org.springframework.http.HttpStatus;

public class OrganizationNotFoundException extends QonnectException{
    public OrganizationNotFoundException(String message, HttpStatus status) {
        super(message, status);
    }
}
