package com.sheetconn.connector.expceptions;

import org.springframework.security.core.AuthenticationException;

public class NoTokenPresentException extends AuthenticationException {
    public NoTokenPresentException(String s) {
        super(s);
    }
}
