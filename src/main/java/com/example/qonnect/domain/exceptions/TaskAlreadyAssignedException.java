package com.example.qonnect.domain.exceptions;

import org.springframework.http.HttpStatus;

public class TaskAlreadyAssignedException extends QonnectException {
    public TaskAlreadyAssignedException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
