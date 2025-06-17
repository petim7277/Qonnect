package com.example.qonnect.domain.exceptions;

import org.springframework.http.HttpStatus;

public class ProjectNotFoundException extends QonnectException{
    public ProjectNotFoundException(String message, HttpStatus status) {
        super(message, status);
    }
}
