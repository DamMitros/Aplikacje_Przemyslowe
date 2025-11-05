package com.example.zad1.exception;

import com.example.zad1.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EmployeeNotFoundException ex, HttpServletRequest req) {
        return build(ex, HttpStatus.NOT_FOUND, req);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleConflict(DuplicateEmailException ex, HttpServletRequest req) {
        return build(ex, HttpStatus.CONFLICT, req);
    }

    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalid(InvalidDataException ex, HttpServletRequest req) {
        return build(ex, HttpStatus.BAD_REQUEST, req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArg(IllegalArgumentException ex, HttpServletRequest req) {
        return build(ex, HttpStatus.BAD_REQUEST, req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex, HttpServletRequest req) {
        return build(ex, HttpStatus.INTERNAL_SERVER_ERROR, req);
    }

    private ResponseEntity<ErrorResponse> build(Exception ex, HttpStatus status, HttpServletRequest req) {
        ErrorResponse body = new ErrorResponse(ex.getMessage(), status.value(), req.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
