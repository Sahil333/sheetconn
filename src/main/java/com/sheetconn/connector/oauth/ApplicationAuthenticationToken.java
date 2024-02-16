package com.sheetconn.connector.oauth;

import io.jsonwebtoken.Claims;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

public class ApplicationAuthenticationToken extends AbstractAuthenticationToken {

    @Getter
    private final Claims claims;
    private String idToken;

    public ApplicationAuthenticationToken(Claims claims, String idToken) {
        super(Collections.emptyList());
        this.claims = claims;
        this.idToken = idToken;
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return idToken;
    }

    @Override
    public Object getPrincipal() {
        return claims.getSubject();
    }

}
