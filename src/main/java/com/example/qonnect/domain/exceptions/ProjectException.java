package com.example.qonnect.domain.exceptions;

import org.springframework.http.HttpStatus;

public class ProjectException extends QonnectException {
    public ProjectException(String message, HttpStatus status) {
        super(message,status);
    }
}
