package com.softjourn.balance.transfer.client.exception;

public class ChannelNotFoundException extends RuntimeException {
    public ChannelNotFoundException() {
    }

    public ChannelNotFoundException(String message) {
        super(message);
    }

    public ChannelNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
