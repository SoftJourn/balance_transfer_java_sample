package com.softjourn.balance.transfer.client.exception;

public class InvalidTransactionProposalException extends RuntimeException {
    public InvalidTransactionProposalException() {
    }

    public InvalidTransactionProposalException(String message) {
        super(message);
    }

    public InvalidTransactionProposalException(String message, Throwable cause) {
        super(message, cause);
    }
}
