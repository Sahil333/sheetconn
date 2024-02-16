package com.sheetconn.connector.expceptions;

public class UserConnectorConfigNotFoundException extends RuntimeException {
    public UserConnectorConfigNotFoundException(String s) {
        super(s);
    }
}
