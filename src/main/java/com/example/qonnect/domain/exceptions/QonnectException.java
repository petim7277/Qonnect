package com.example.qonnect.domain.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
@Setter
@Getter
public class QonnectException extends RuntimeException {
    private String message;
    private HttpStatus status;

    public QonnectException(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }

}
