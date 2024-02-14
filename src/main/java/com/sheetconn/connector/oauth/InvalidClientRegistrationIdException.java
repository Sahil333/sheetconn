package com.sheetconn.connector.oauth;

public class InvalidClientRegistrationIdException extends IllegalArgumentException {

    /**
     * @param message the exception message
     */
    InvalidClientRegistrationIdException(String message) {
        super(message);
    }

}
