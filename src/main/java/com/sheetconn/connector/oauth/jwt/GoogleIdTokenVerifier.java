package com.sheetconn.connector.oauth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sheetconn.connector.expceptions.InvalidAudienceException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;

import java.util.Set;

@Slf4j
public class GoogleIdTokenVerifier {

    private final GoogleSigningKeyLocator keyLocator;
    private final Set<String> audiences;

    public GoogleIdTokenVerifier(ObjectMapper mapper, Set<String> audiences) {
        this.keyLocator = new GoogleSigningKeyLocator(mapper);
        this.audiences = audiences;
    }

    public Claims tokenValidation(String idToken) {
        Claims claims = (Claims) Jwts.parser().keyLocator(keyLocator)
                .requireIssuer("https://accounts.google.com")
                .build()
                .parse(idToken)
                .getPayload();
        if(SetUtils.intersection(audiences, claims.getAudience()).isEmpty()) {
            throw new InvalidAudienceException("The target audience is not this application");
        }
        return claims;
    }
}
