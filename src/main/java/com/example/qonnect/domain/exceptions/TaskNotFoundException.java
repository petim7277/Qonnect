package com.example.qonnect.domain.exceptions;

import org.springframework.http.HttpStatus;

public class TaskNotFoundException extends QonnectException{
    public TaskNotFoundException(String message, HttpStatus httpStatus) {
        super(message,httpStatus);
    }
}
