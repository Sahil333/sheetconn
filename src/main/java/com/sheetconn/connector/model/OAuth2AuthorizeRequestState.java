package com.sheetconn.connector.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Table(name = "oauth2_authorize_request_state")
public class OAuth2AuthorizeRequestState {

    @Id
    @Column(name = "state")
    private String state;

    @Column(name = "email")
    private String email;
}
