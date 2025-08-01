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
    public static final String ACCESS_DENIED = "You are not permitted to created  project";
    public static final String PROJECT_EXIST_ALREADY = "Project already exist";
    public static final String ORGANIZATION_NOT_FOUND = "Organization not found";
    public static final String ORGANIZATION_ALREADY_EXISTS = "Organization already exists";
    public static final String USER_NOT_ENABLED = "User is not enabled";
    public static final String PROJECT_NOT_FOUND = "Project not found";
    public static final String PROJECT_ID_IS_REQUIRED = "Project ID is required";
    public static final String USER_ALREADY_ASSIGNED_TO_PROJECT ="User is already assigned to the project" ;
    public static final String TASK_NOT_FOUND = "Task not found";
    public static final String TASK_ALREADY_EXISTS = "Task already exist";
    public static final String TASK_NOT_FOUND_IN_PROJECT = "Task not found in project";
    public static final String DUE_DATE_INVALID = "Due date cannot be in the past";
    public static final String ACCESS_DENIED_TO_VIEW_TASK = "You are not permitted to view task";
    public static final String ACCESS_DENIED_TO_ORGANIZATION = "You don't belong to this organization";
    public static final String ONLY_DEVELOPER_CAN_BE_ASSIGNED_TASK = "Only developer can be assigned task";
    public static final String ACCESS_DENIED_TO_ASSIGN_TASK = "you are not permitted to assign task";
    public static final String ONLY_DEVELOPER_CAN_PICK_TASK = "only developer can pick task";
    public static final String TASK_ALREADY_ASSIGNED = "Task Already Assigned";
    public static final String BUG_NOT_FOUND = "Bug not found";
    public static final String BUG_SEVERITY_IS_REQUIRED = "Bug severity is required";
    public static final String BUG_STATUS_IS_REQUIRED = "Bug status is required";
    public static final String ACCESS_DENIED_TO_REPORT_BUG = "Only QA can report bug";
    public static final String ACCESS_DENIED_TO_ASSIGN_BUG = "Only Admins or QA Engineers can assign bugs";


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
