package com.sheetconn.connector.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Base64;

public class JwtUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static ObjectNode parseForPayload(String accessToken) {
        String[] chunks = accessToken.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String payloadString = new String(decoder.decode(chunks[1]));
        try {
            return (ObjectNode) mapper.readTree(payloadString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getSubject(String accessToken) {
        ObjectNode payload = parseForPayload(accessToken);
        return payload.get("sub").asText();
    }
}
