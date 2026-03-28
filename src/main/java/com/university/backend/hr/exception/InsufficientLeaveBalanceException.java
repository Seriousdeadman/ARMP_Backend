package com.university.backend.hr.exception;

public class InsufficientLeaveBalanceException extends RuntimeException {

    public InsufficientLeaveBalanceException(String message) {
        super(message);
    }
}
