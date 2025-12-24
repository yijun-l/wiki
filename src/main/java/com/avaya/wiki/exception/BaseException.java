package com.avaya.wiki.exception;

public abstract class BaseException extends RuntimeException {
    private final int code;

    public BaseException(String message, int code) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
