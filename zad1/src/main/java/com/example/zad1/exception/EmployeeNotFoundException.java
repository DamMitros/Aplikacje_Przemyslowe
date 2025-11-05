package com.example.zad1.exception;

public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(String email) {
        super("Employee not found for email: " + email);
    }
}
