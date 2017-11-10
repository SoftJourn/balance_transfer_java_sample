package com.softjourn.balance.transfer.client.exception;

public class AdminNotFoundException extends RuntimeException {
    public AdminNotFoundException() {
    }

    public AdminNotFoundException(String message) {
        super(message);
    }

    public AdminNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
