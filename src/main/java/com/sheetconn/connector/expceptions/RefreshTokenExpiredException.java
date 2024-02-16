package com.sheetconn.connector.expceptions;

public class RefreshTokenExpiredException extends RuntimeException {
    public RefreshTokenExpiredException(String s) {
        super(s);
    }
}
