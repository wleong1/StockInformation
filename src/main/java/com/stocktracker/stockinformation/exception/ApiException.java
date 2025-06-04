package com.stocktracker.stockinformation.exception;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super("API Error: " + message);
    }
}
