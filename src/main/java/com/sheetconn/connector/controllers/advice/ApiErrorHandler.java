package com.sheetconn.connector.controllers.advice;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiErrorHandler {

    @ExceptionHandler(value = {Exception.class})
    public ErrorResponse generalHandler(Exception ex, HttpServletResponse resp) {
        resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCodeMap.getErrorCode(ex.getClass()),
                ex.getMessage()
        );
    }
}
