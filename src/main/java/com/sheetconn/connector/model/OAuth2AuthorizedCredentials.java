package com.sheetconn.connector.model;

import java.io.Serializable;
import java.time.ZonedDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "oauth2_authorized_client")
@Getter
public class OAuth2AuthorizedCredentials {

    @EmbeddedId
    private CredentialKey credentialId;

    @Column(name="access_token_type")
    private String accessTokenType;

    @Column(name="access_token_value", columnDefinition = "blob")
    private String accessTokenValue;

    @Column(name="access_token_issued_at")
    private ZonedDateTime accessTokenIssuedAt;

    @Column(name="access_token_expires_at")
    private ZonedDateTime accessTokenExpiresAt;

    @Column(name = "access_token_scopes", columnDefinition = "varchar(1000)")
    private String accessTokenScopes;
    
    @Column(name = "refresh_token_value", columnDefinition = "blob")
    private String refreshTokenValue;

    @Column(name = "refresh_token_issued_at")
    private ZonedDateTime refreshTokenIssuedAt;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;
    
    @EqualsAndHashCode
    @NoArgsConstructor
    @Getter
    @Embeddable
    public static class CredentialKey implements Serializable {

        @Column(name="client_registration_id", columnDefinition = "varchar(100)")
        private String clientRegistrationId;

        @Column(name="principal_name", columnDefinition = "varchar(200)")
        private String principalName;
    }
}
