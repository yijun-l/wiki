package com.avaya.wiki.exception;

import com.avaya.wiki.response.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CommonResponse<?>> handleResourceNotFoundException(ResourceNotFoundException exception) {
        return ResponseEntity
                .status(exception.getCode())
                .body(CommonResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<?>> handleException(Exception exception) {
        return ResponseEntity
                .status(500)
                .body(CommonResponse.error("Internal server error"));
    }
}
