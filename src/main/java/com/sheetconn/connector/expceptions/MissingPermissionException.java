package com.sheetconn.connector.expceptions;

import org.springframework.security.core.AuthenticationException;

public class MissingPermissionException extends AuthenticationException {
    public MissingPermissionException(String message) {
        super(message);
    }
}
