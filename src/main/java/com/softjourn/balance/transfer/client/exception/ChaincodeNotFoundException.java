package com.softjourn.balance.transfer.client.exception;

public class ChaincodeNotFoundException extends RuntimeException {

    public ChaincodeNotFoundException() {
    }

    public ChaincodeNotFoundException(String message) {
        super(message);
    }

    public ChaincodeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
