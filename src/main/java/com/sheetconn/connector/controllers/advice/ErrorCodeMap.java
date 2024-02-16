package com.sheetconn.connector.controllers.advice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sheetconn.connector.expceptions.InvalidAudienceException;
import com.sheetconn.connector.expceptions.InvalidTokenException;
import com.sheetconn.connector.expceptions.MissingPermissionException;
import com.sheetconn.connector.expceptions.NoTokenPresentException;
import com.sheetconn.connector.expceptions.RefreshTokenExpiredException;
import com.sheetconn.connector.expceptions.UserConnectorConfigNotFoundException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;

import java.util.HashMap;
import java.util.Map;

public class ErrorCodeMap {

    private static final Map<Class<? extends Exception>, Integer> errorCodeMap = new HashMap<>();

    static {
        errorCodeMap.put(InvalidAudienceException.class, 1001);
        errorCodeMap.put(InvalidTokenException.class, 1002);
        errorCodeMap.put(MissingPermissionException.class, 1003);
        errorCodeMap.put(NoTokenPresentException.class, 1004);
        errorCodeMap.put(RefreshTokenExpiredException.class, 1005);
        errorCodeMap.put(UserConnectorConfigNotFoundException.class, 1006);
        errorCodeMap.put(JsonProcessingException.class, 1007);
        errorCodeMap.put(UnsupportedJwtException.class, 1008);
        errorCodeMap.put(MalformedJwtException.class, 1009);
        errorCodeMap.put(SecurityException.class, 1010);
        errorCodeMap.put(ExpiredJwtException.class, 1011);
        errorCodeMap.put(IllegalArgumentException.class, 1012);
    }

    public static Integer getErrorCode(Class<? extends Exception> ex) {
        return errorCodeMap.getOrDefault(ex, 9999);
    }
}
