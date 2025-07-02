package com.example.qonnect.domain.exceptions;

import org.springframework.http.HttpStatus;

public class ProjectAlreadyExistException extends QonnectException {
    public ProjectAlreadyExistException(String message, HttpStatus status) {
        super(message, status);
    }
}
