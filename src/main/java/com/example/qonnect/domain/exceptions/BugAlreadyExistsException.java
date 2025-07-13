package com.example.qonnect.domain.exceptions;

import org.springframework.http.HttpStatus;

public class BugAlreadyExistsException extends QonnectException{
    public BugAlreadyExistsException(String message, HttpStatus status) {
        super(message, status);
    }
}
