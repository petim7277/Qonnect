package com.example.qonnect.domain.exceptions;

import org.springframework.http.HttpStatus;

public class TaskAlreadyExistException extends QonnectException {
    public TaskAlreadyExistException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);

    }
}
