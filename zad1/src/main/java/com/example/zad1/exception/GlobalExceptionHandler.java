package com.example.zad1.exception;

import com.example.zad1.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

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

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponse> handleStorage(FileStorageException ex, HttpServletRequest req) {
        return build(ex, HttpStatus.INTERNAL_SERVER_ERROR, req);
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFile(InvalidFileException ex, HttpServletRequest req) {
        return build(ex, HttpStatus.BAD_REQUEST, req);
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFileNotFound(FileNotFoundException ex, HttpServletRequest req) {
        return build(ex, HttpStatus.NOT_FOUND, req);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleTooLarge(MaxUploadSizeExceededException ex, HttpServletRequest req) {
        Long max = null;
        try { max = ex.getMaxUploadSize(); } catch (Throwable ignored) {}
        String msg = "Przekroczono maksymalny rozmiar przesyłki" + (max != null ? ": maksymalnie " + human(max) : "");
        return build(new Exception(msg), HttpStatus.PAYLOAD_TOO_LARGE, req);
    }

    private String human(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double kb = bytes / 1024.0;
        if (kb < 1024) return String.format("%.1f KB", kb);
        double mb = kb / 1024.0;
        if (mb < 1024) return String.format("%.1f MB", mb);
        double gb = mb / 1024.0;
        return String.format("%.1f GB", gb);
    }

    private ResponseEntity<ErrorResponse> build(Exception ex, HttpStatus status, HttpServletRequest req) {
        ErrorResponse body = new ErrorResponse(ex.getMessage(), status.value(), req.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
