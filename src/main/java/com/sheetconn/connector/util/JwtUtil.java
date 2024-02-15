package com.sheetconn.connector.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwe;
import io.jsonwebtoken.Jwts;

import java.util.Base64;

public class JwtUtil {



    public static Claims parseForPayload(String accessToken) {
        return (Claims) Jwts.parser().build().parse(accessToken).getPayload();
    }

//    public static Claims verifyGoogleIdToken(String accessToken) {
//
//    }
}
