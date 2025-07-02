package com.example.qonnect.domain.exceptions;

import org.springframework.http.HttpStatus;

public class OrganizationAlreadyExistsException extends QonnectException {
    public OrganizationAlreadyExistsException(String organizationAlreadyExists, HttpStatus httpStatus) {
        super(organizationAlreadyExists, httpStatus);
    }
}
