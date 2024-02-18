package com.sheetconn.connector.oauth;

import io.jsonwebtoken.Claims;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthorizationCodeAuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;

import java.util.Map;

public class OAuth2AuthenticationToken extends OAuth2AuthorizationCodeAuthenticationToken {

    private final Claims claims; // acquired from the id_token of the authorized client, not necessarily same user as the initiator user
    public OAuth2AuthenticationToken(Claims claims,
                                     ClientRegistration clientRegistration,
                                     OAuth2AuthorizationExchange authorizationExchange,
                                     OAuth2AccessToken accessToken,
                                     OAuth2RefreshToken refreshToken) {
        super(clientRegistration, authorizationExchange, accessToken, refreshToken);
        this.claims = claims;
    }

    @Override
    public Object getPrincipal() {
        return claims.getSubject();
    }

    public Claims getClaims() {
        return claims;
    }
}
