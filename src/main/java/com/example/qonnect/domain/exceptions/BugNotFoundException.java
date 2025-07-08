package com.example.qonnect.domain.exceptions;

import org.springframework.http.HttpStatus;

public class BugNotFoundException extends QonnectException{



    public BugNotFoundException(String message, HttpStatus status) {
        super(message, status);
    }


}
