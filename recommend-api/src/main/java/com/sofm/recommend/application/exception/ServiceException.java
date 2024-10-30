package com.sofm.recommend.application.exception;

public class ServiceException extends RuntimeException {
    private final int code;
    private final String message;


    public ServiceException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public ServiceException(String message, int code, String message1) {
        super(message);
        this.code = code;
        this.message = message1;
    }

    public ServiceException(String message, Throwable cause, int code, String message1) {
        super(message, cause);
        this.code = code;
        this.message = message1;
    }

    public ServiceException(Throwable cause, int code, String message) {
        super(cause);
        this.code = code;
        this.message = message;
    }

    public ServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, int code, String message1) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.code = code;
        this.message = message1;
    }

    public static ServiceException of(int code, String message) {
        return new ServiceException(code, message);
    }
}