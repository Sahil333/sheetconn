package com.sheetconn.connector.oauth;

import com.sheetconn.connector.util.JwtUtil;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthorizationCodeAuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;

public class OAuth2AuthenticationToken extends OAuth2AuthorizationCodeAuthenticationToken {

    public OAuth2AuthenticationToken(ClientRegistration clientRegistration, OAuth2AuthorizationExchange authorizationExchange) {
        super(clientRegistration, authorizationExchange);
    }

    @Override
    public Object getPrincipal() {
        return JwtUtil.getSubject(this.getAccessToken().getTokenValue());
    }
}
