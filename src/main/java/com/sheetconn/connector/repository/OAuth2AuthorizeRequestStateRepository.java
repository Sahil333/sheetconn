package com.sheetconn.connector.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import com.sheetconn.connector.model.OAuth2AuthorizeRequestState;

public interface OAuth2AuthorizeRequestStateRepository extends 
    JpaRepository<OAuth2AuthorizeRequestState, String>, 
    AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    
}
