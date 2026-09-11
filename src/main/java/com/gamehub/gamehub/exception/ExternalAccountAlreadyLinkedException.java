package com.gamehub.gamehub.exception;

public class ExternalAccountAlreadyLinkedException extends RuntimeException {

    public ExternalAccountAlreadyLinkedException(String message) {
        super(message);
    }
}
