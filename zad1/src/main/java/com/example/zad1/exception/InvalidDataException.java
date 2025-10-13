package com.example.zad1.exception;

public class InvalidDataException extends Exception {
    public InvalidDataException(String message) {
        super(message);
    }
    public InvalidDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
