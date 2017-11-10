package com.softjourn.balance.transfer.client.exception;

public class OrganizationNotFoundException extends RuntimeException {

    public OrganizationNotFoundException() {
    }

    public OrganizationNotFoundException(String message) {
        super(message);
    }

    public OrganizationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
