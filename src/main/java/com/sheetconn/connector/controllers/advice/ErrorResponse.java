package com.sheetconn.connector.controllers.advice;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorResponse {

    private HttpStatus status;
    private Integer errorCode;
    private String message;
    private String details;

    private String traceId;

    public ErrorResponse(HttpStatus status, Integer errorCode, String message) {
        this(status, errorCode, message, null, null);
    }

    public ErrorResponse(HttpStatus status, Integer errorCode, String message, String details, String traceId) {
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.details = details;
        this.traceId = traceId;
    }
}
