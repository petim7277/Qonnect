package com.example.qonnect.domain.validators;

import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;

import java.util.regex.Pattern;

public class InputValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    public static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.EMPTY_EMAIL);
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException(ErrorMessages.INVALID_EMAIL_FORMAT);
        }
    }

    public static void validateName(String name, String fieldName) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.emptyField(fieldName));
        }
        if (name.length() < 2 || name.length() > 50) {
            throw new IllegalArgumentException(ErrorMessages.invalidLength(fieldName));
        }
        if (!name.matches("^[A-Za-z\\s'-]+$")) {
            throw new IllegalArgumentException(ErrorMessages.invalidCharacters(fieldName));
        }
    }

    public static void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.EMPTY_PASSWORD);
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException(ErrorMessages.PASSWORD_TOO_SHORT);
        }
        if (!password.matches(".*[A-Z].*") ||
                !password.matches(".*[a-z].*") ||
                !password.matches(".*\\d.*") ||
                !password.matches(".*[!@#$%^&*()].*")) {
            throw new IllegalArgumentException(ErrorMessages.WEAK_PASSWORD);
        }
    }

    public static void validateRole(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.EMPTY_ROLE);
        }
    }

    public static void validateInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid input");
        }
    }
}
