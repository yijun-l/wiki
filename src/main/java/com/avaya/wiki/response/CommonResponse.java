package com.avaya.wiki.response;

public class CommonResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public static <T> CommonResponse<T> success(T data) {
        CommonResponse<T> commonResponse = new CommonResponse<>();
        commonResponse.setSuccess(true);
        commonResponse.setData(data);
        return commonResponse;
    }

    public static <T> CommonResponse<T> error(String message) {
        CommonResponse<T> commonResponse = new CommonResponse<>();
        commonResponse.setSuccess(false);
        commonResponse.setMessage(message);
        return commonResponse;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }


}
