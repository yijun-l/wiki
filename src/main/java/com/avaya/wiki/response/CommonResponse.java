package com.avaya.wiki.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CommonResponse<T> {
    private boolean success;
    private String message;
    private T data;
}
