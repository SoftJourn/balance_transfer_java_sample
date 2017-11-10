package com.softjourn.balance.transfer.client.exception;

public class ChannelAlreadyExistsException extends RuntimeException{
    public ChannelAlreadyExistsException() {
    }

    public ChannelAlreadyExistsException(String message) {
        super(message);
    }

    public ChannelAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
