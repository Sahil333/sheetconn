package com.sheetconn.connector.repository;

import com.sheetconn.connector.model.OAuth2AuthorizeRequestState;
import org.springframework.data.jpa.repository.JpaRepository;

// Need to be promoted to AuthorizationRequestRepository<OAuth2AuthorizationRequest> as default is SessionBased
// which will not work in multiple server instances behind load balancer else will need sticky session enabling
public interface OAuth2AuthorizeRequestStateRepository extends JpaRepository<OAuth2AuthorizeRequestState, String> {

}
