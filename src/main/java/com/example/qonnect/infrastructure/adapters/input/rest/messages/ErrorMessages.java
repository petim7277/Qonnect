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
    public static final String OTP_NOT_FOUND = "otp not found";
    public static final String OTP_ALREADY_USED = "otp already used";
    public static final String OTP_ALREADY_EXPIRED = "otp already expired";

    public static final String EMPTY_EMAIL = "Email cannot be empty";
    public static final String INVALID_EMAIL_FORMAT = "Email format is invalid";
    public static final String EMPTY_PASSWORD = "Password cannot be empty";
    public static final String PASSWORD_TOO_SHORT = "Password must be at least 8 characters long";
    public static final String WEAK_PASSWORD = "Password must include uppercase, lowercase, number, and special character";
    public static final String EMPTY_ROLE = "Role cannot be empty";
    public static final String INVALID_ROLE = "Invalid role";
    public static final String INVALID_OTP = "Invalid otp";
    public static final String INCORRECT_OLD_PASSWORD = "Old password is incorrect";
    public static final String NEW_PASSWORD_SAME_AS_OLD = "New password must be different from the old password";
    public static final String ORGANIZATION_NOT_FOUND = "Organization not found";
    public static final String ORGANIZATION_ALREADY_EXISTS = "Organization already exists";
    public static final String ACCESS_DENIED = "Only admins can invite users.";
    public static final String USER_NOT_ENABLED = "User is not enabled";


    public static String roleNotFound(String roleName) {
        return String.format("Role '%s' does not exist", roleName);
    }
    public static String emptyField(String fieldName) {
        return fieldName + " cannot be empty";
    }

    public static String invalidLength(String fieldName) {
        return fieldName + " must be between 2 and 50 characters";
    }

    public static String invalidCharacters(String fieldName) {
        return fieldName + " contains invalid characters";
    }

}
