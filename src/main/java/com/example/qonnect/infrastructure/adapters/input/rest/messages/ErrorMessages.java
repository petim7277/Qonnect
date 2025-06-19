package com.example.qonnect.infrastructure.adapters.input.rest.messages;

public class ErrorMessages {
    public static final String USER_NOT_FOUND = "User not found";
    public static final String AUTHENTICATION_FAILED = "Authentication failed";
    public static final String EMPTY_INPUT_ERROR = "Field cannot be empty";
    public static final String USER_EXISTS_ALREADY = "User already exists";
    public static final String INVALID_CREDENTIALS = "invalid credentials";
    public static final String ERROR_FETCHING_USER_INFORMATION = "error fetching user information";
    public static final String PASSWORD_RESET_FAILED = "password reset failed";
    public static final String INVALID_REQUEST = "invalid request";



    public static String roleNotFound(String roleName) {
        return String.format("Role '%s' does not exist", roleName);
    }
}
