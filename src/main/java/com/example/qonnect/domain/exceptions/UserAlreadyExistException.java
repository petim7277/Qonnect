package com.example.qonnect.domain.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

public class UserAlreadyExistException extends QonnectException {

    public UserAlreadyExistException(String message, HttpStatus status) {
        super(message, status);
    }
}
