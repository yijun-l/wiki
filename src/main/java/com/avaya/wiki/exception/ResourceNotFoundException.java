package com.avaya.wiki.exception;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(String message) {
        super(message, 404);
    }
}
