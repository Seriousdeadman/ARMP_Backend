package com.university.backend.hr.web;

import com.university.backend.hr.exception.InsufficientLeaveBalanceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(basePackages = "com.university.backend.hr")
public class HrExceptionHandler {

    @ExceptionHandler(InsufficientLeaveBalanceException.class)
    public ResponseEntity<Map<String, String>> handleInsufficientLeave(InsufficientLeaveBalanceException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}
