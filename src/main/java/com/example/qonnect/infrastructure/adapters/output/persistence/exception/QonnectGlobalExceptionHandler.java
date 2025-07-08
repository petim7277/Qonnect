package com.example.qonnect.infrastructure.adapters.output.persistence.exception;

import com.example.qonnect.domain.exceptions.*;
import com.example.qonnect.infrastructure.adapters.input.rest.data.responses.ErrorResponse;
import org.apache.http.auth.InvalidCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class QonnectGlobalExceptionHandler {
    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistException userAlreadyExistException) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                userAlreadyExistException.getMessage(),
                Instant.now()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException illegalArgumentException) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                illegalArgumentException.getMessage(),
                Instant.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TaskAlreadyAssignedException.class)
    public ResponseEntity<ErrorResponse> handleTaskAlreadyAssigned(TaskAlreadyAssignedException taskAlreadyAssignedException) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                taskAlreadyAssignedException.getMessage(),
                Instant.now()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTaskNotFound(TaskNotFoundException taskNotFoundException) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                taskNotFoundException.getMessage(),
                Instant.now()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OrganizationAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleOrganizationAlreadyExistArgument(OrganizationAlreadyExistsException organizationAlreadyExistsException) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                organizationAlreadyExistsException.getMessage(),
                Instant.now()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException authenticationException) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                authenticationException.getMessage(),
                Instant.now()
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(IdentityManagementException.class)
    public ResponseEntity<ErrorResponse> handleIdentityManagementException(IdentityManagementException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getStatus().value(),
                ex.getMessage(),
                Instant.now()
        );
        return new ResponseEntity<>(error, ex.getStatus());
    }


    @ExceptionHandler(BugAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleBugAlreadyExists(BugAlreadyExistsException bugAlreadyExistsException) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                bugAlreadyExistsException.getMessage(),
                Instant.now()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProjectNotFound(ProjectNotFoundException projectNotFoundException) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                projectNotFoundException.getMessage(),
                Instant.now()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ProjectAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleProjectAlreadyExist(ProjectAlreadyExistException projectAlreadyExistException) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                projectAlreadyExistException.getMessage(),
                Instant.now()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException accessDeniedException) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                accessDeniedException.getMessage(),
                Instant.now()
        );
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

  @ExceptionHandler(OtpException.class)
    public ResponseEntity<ErrorResponse> handleOtpException(OtpException otpException) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                otpException.getMessage(),
                Instant.now()
        );
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(OtpNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOtpNotFoundException(OtpNotFoundException otpNotFoundException) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                otpNotFoundException.getMessage(),
                Instant.now()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }





    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException userNotFoundException) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                userNotFoundException.getMessage(),
                Instant.now()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException invalidCredentialsException) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                invalidCredentialsException.getMessage(),
                Instant.now()
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(OrganizationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrganizationNotFound(OrganizationNotFoundException organizationNotFoundException) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                organizationNotFoundException.getMessage(),
                Instant.now()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

}
