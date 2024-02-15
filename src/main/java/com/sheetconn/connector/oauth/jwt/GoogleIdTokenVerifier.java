package com.sheetconn.connector.oauth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GoogleIdTokenVerifier {

    private final GoogleSigningKeyLocator keyLocator;

    GoogleIdTokenVerifier(ObjectMapper mapper) {
        this.keyLocator = new GoogleSigningKeyLocator(mapper);
    }

    public boolean isTokenValid(String idToken) {
        boolean valid = false;
        try {
            Jwts.parser().keyLocator(keyLocator)
                    .requireIssuer("https://accounts.google.com")
                    .build()
                    .parse(idToken);
            valid = true;
        } catch (Exception e) {
            log.error("Failed to verify the token validity - ", e);
        }
        return valid;
    }

    public Claims getClaims(String idToken) {
        try {
            return (Claims) Jwts.parser().keyLocator(keyLocator)
                    .requireIssuer("https://accounts.google.com")
                    .build()
                    .parse(idToken)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }
}
