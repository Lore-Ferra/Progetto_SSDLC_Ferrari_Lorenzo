package com.bittercode.model;

import java.io.IOException;

import com.bittercode.constant.ResponseCode;

public class StoreException extends IOException {

    private final String errorCode;
    private final String errorMessage;
    private final int statusCode;

    public StoreException(String errorMessage) {
        super(errorMessage);
        this.errorCode = "BAD_REQUEST";
        this.statusCode = 400;
        this.errorMessage = errorMessage;
    }

    public StoreException(ResponseCode errorCodes) {
        super(errorCodes.getMessage());
        this.statusCode = errorCodes.getCode();
        this.errorMessage = errorCodes.getMessage();
        this.errorCode = errorCodes.name();
    }

    public StoreException(String errroCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errroCode;
        this.errorMessage = errorMessage;
        this.statusCode = 422;
    }

    public StoreException(int statusCode, String errorCode, String errorMessage) {
        super(errorMessage);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
