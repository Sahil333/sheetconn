package com.sheetconn.connector.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sheetconn.connector.controllers.dto.PostgresConfig;
import com.sheetconn.connector.controllers.dto.SheetProvider;
import com.sheetconn.connector.model.ConnectorType;
import com.sheetconn.connector.model.User;
import com.sheetconn.connector.model.UserConnectorConfig;
import com.sheetconn.connector.oauth.MultiConnectRefreshTokenResponseClient;
import com.sheetconn.connector.oauth.OAuth2AuthenticationToken;
import com.sheetconn.connector.repository.UserConnectorConfigRepository;
import com.sheetconn.connector.repository.UserRepository;
import com.sheetconn.connector.expceptions.RefreshTokenExpiredException;
import com.sheetconn.connector.expceptions.UserConnectorConfigNotFoundException;
import io.jsonwebtoken.Claims;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ConnectorRegistryService {
    
    private final UserConnectorConfigRepository connectorConfigRepository;
    private final UserRepository userRepository;
    private final ClientRegistrationRepository clientRegistrationRepository;

    private final OAuth2AccessTokenResponseClient<OAuth2RefreshTokenGrantRequest> refreshTokenResponseClient
            = new MultiConnectRefreshTokenResponseClient();

    public ConnectorRegistryService(UserConnectorConfigRepository connectorConfigRepository,
                                    UserRepository userRepository,
                                    ClientRegistrationRepository clientRegistrationRepository) {
        this.connectorConfigRepository = connectorConfigRepository;
        this.userRepository = userRepository;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    public void registerPostgresConnector(PostgresConfig postgresConfig, String uid) {
        ObjectNode connectorConfig = JsonNodeFactory.instance.objectNode();
        connectorConfig.put(ConnectorConfigConstants.Postgres.HOST, postgresConfig.getHost());
        connectorConfig.put(ConnectorConfigConstants.Postgres.PORT, postgresConfig.getPort());
        connectorConfig.put(ConnectorConfigConstants.Postgres.DATABASE, postgresConfig.getDatabase());
        connectorConfig.put(ConnectorConfigConstants.Postgres.USER, postgresConfig.getUser());
        connectorConfig.put(ConnectorConfigConstants.Postgres.PASSWORD, postgresConfig.getPassword());

        UserConnectorConfig config = UserConnectorConfig.builder()
            .connectorConfig(connectorConfig)
            .type(ConnectorType.POSTGRESQL)
            .user(userRepository.getReferenceById(uid))
            .id(UUID.randomUUID().toString())
            .build();

        connectorConfigRepository.save(config);
    }

    public UserConnectorConfig getConfig(String connectorId) {
        return connectorConfigRepository.findById(connectorId)
            .orElseThrow(() -> new RuntimeException("Config not found for connectorId: " + connectorId));
    }

    public List<UserConnectorConfig> getConfigs(String uid) {
        return connectorConfigRepository.findAllByUser(userRepository.getReferenceById(uid));
    }

    public void registerOAuthConnect(String uid, OAuth2AuthenticationToken authenticationToken) {
        ObjectNode connectorConfig = JsonNodeFactory.instance.objectNode();
        connectorConfig.put(ConnectorConfigConstants.OAuth.CLIENT_REGISTRATION_ID, authenticationToken.getClientRegistration().getClientId());
        connectorConfig.put(ConnectorConfigConstants.OAuth.ACCESS_TOKEN, authenticationToken.getAccessToken().getTokenValue());
        connectorConfig.put(ConnectorConfigConstants.OAuth.ACCESS_TOKEN_EXPIRES_AT, authenticationToken.getAccessToken().getExpiresAt().getEpochSecond());
        connectorConfig.put(ConnectorConfigConstants.OAuth.REFRESH_TOKEN, authenticationToken.getRefreshToken().getTokenValue());

        if (authenticationToken.getRefreshToken().getExpiresAt() == null) {
            connectorConfig.put(ConnectorConfigConstants.OAuth.REFRESH_TOKEN_EXPIRES_AT, Long.MAX_VALUE);
        } else {
            connectorConfig.put(ConnectorConfigConstants.OAuth.REFRESH_TOKEN_EXPIRES_AT, authenticationToken.getRefreshToken().getExpiresAt().getEpochSecond());
        }

        Claims claims = authenticationToken.getClaims();
        connectorConfig.put(ConnectorConfigConstants.OAuth.SUBJECT, claims.getSubject());
        connectorConfig.put(ConnectorConfigConstants.OAuth.EMAIL, claims.get("email", String.class));
        connectorConfig.put(ConnectorConfigConstants.OAuth.NAME, claims.get("name", String.class));
        connectorConfig.put(ConnectorConfigConstants.OAuth.PICTURE, claims.get("picture", String.class));
        connectorConfig.put(ConnectorConfigConstants.OAuth.PHONE_NUMBER, claims.get("phone_number", String.class));
        ConnectorType type = ConnectorType
                .getConnectorTypeFromName(authenticationToken.getClientRegistration().getClientName());
        User user = userRepository.getReferenceById(uid);

        List<UserConnectorConfig> userConnectors = connectorConfigRepository.findAllByUserAndType(user, type);

        UserConnectorConfig existingConfig = findExistingOAuthConnector(userConnectors, claims.getSubject());

        UserConnectorConfig toSave;

        if(existingConfig == null) {
            toSave = UserConnectorConfig.builder()
                .user(user)
                .id(UUID.randomUUID().toString())
                .type(type)
                .connectorConfig(connectorConfig)
                .build();
        } else {
            toSave = existingConfig;
            toSave.setConnectorConfig(connectorConfig);
        }

        connectorConfigRepository.save(toSave);
    }

    private UserConnectorConfig findExistingOAuthConnector(List<UserConnectorConfig> userConnectors, String subject) {
        if(userConnectors.isEmpty()) return null;
        for(UserConnectorConfig oauthConnector : userConnectors) {
            if(oauthConnector.getConnectorConfig().get(ConnectorConfigConstants.OAuth.SUBJECT).asText().equals(subject)) {
                return oauthConnector;
            }
        }
        return null;
    }

    public OAuth2AuthorizedClient fetchPrimarySheetAuthorizedClient(SheetProvider provider, String userId) {
        ConnectorType type = provider.getConnectorType();
        User user = userRepository.getReferenceById(userId);

        List<UserConnectorConfig> configs = connectorConfigRepository.findAllByUserAndType(user, type);

        if(configs.isEmpty()) {
            throw new UserConnectorConfigNotFoundException("No connector config found");
        }

        for(UserConnectorConfig config : configs) {
            if(config.getConnectorConfig().get(ConnectorConfigConstants.OAuth.SUBJECT).asText()
                    .equals(userId)) {
                return buildOAuthClient(config);
            }
        }

        throw new UserConnectorConfigNotFoundException("No sheet connect config found");
    }

    public OAuth2AuthorizedClient fetchOAuth2AuthorizedClient(String connectorId) {
        Optional<UserConnectorConfig> config = connectorConfigRepository.findById(connectorId);

        if(config.isEmpty()) {
            throw new UserConnectorConfigNotFoundException("Connector config not found for exception");
        }

        if(!config.get().getType().getIsOAuth()) {
            throw new IllegalArgumentException("Provided connector is not OAuth type");
        }

        JsonNode connectorConfig = config.get().getConnectorConfig();

        long accessTokenExpiresAt = connectorConfig.get(ConnectorConfigConstants.OAuth.ACCESS_TOKEN_EXPIRES_AT).asLong();
        long refreshTokenExpiresAt = connectorConfig.get(ConnectorConfigConstants.OAuth.REFRESH_TOKEN_EXPIRES_AT).asLong();

        // 5 minutes buffer till expiry of access token
        if(accessTokenExpiresAt < ZonedDateTime.now(ZoneId.of("Z")).toEpochSecond() - 300) {
            if(refreshTokenExpiresAt < ZonedDateTime.now(ZoneId.of("Z")).toEpochSecond() - 10) {
                connectorConfigRepository.deleteById(connectorId);
                throw new RefreshTokenExpiredException("Refresh token has expired, please re-authorize");
            } else {
                refreshAccessToken(config.get());
            }
        }

        return buildOAuthClient(config.get());
    }

    public void refreshAccessToken(UserConnectorConfig config) {
        ClientRegistration clientRegistration = clientRegistrationRepository.
                findByRegistrationId(config.getType().getConnectorType());
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                config.getConnectorConfig().get(ConnectorConfigConstants.OAuth.ACCESS_TOKEN).asText(),
                null,
                null,
                clientRegistration.getScopes());
        OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(
                config.getConnectorConfig().get(ConnectorConfigConstants.OAuth.REFRESH_TOKEN).asText(),
                null,
                Instant.ofEpochSecond(config.getConnectorConfig().get(ConnectorConfigConstants.OAuth.REFRESH_TOKEN_EXPIRES_AT).asLong())
        );
        OAuth2RefreshTokenGrantRequest refreshTokenGrantRequest =
                new OAuth2RefreshTokenGrantRequest(clientRegistration, accessToken, refreshToken);

        OAuth2AccessTokenResponse accessTokenResponse =
                refreshTokenResponseClient.getTokenResponse(refreshTokenGrantRequest);

        ObjectNode connectorConfig = (ObjectNode) config.getConnectorConfig();

        connectorConfig.put(ConnectorConfigConstants.OAuth.ACCESS_TOKEN, accessTokenResponse.getAccessToken().getTokenValue());
        connectorConfig.put(ConnectorConfigConstants.OAuth.ACCESS_TOKEN_EXPIRES_AT, accessTokenResponse.getAccessToken().getExpiresAt().getEpochSecond());
        connectorConfig.put(ConnectorConfigConstants.OAuth.REFRESH_TOKEN, accessTokenResponse.getRefreshToken().getTokenValue());

        if (accessTokenResponse.getRefreshToken().getExpiresAt() == null) {
            connectorConfig.put(ConnectorConfigConstants.OAuth.REFRESH_TOKEN_EXPIRES_AT, Long.MAX_VALUE);
        } else {
            connectorConfig.put(ConnectorConfigConstants.OAuth.REFRESH_TOKEN_EXPIRES_AT, accessTokenResponse.getRefreshToken().getExpiresAt().getEpochSecond());
        }

        connectorConfigRepository.save(config);
    }

    private OAuth2AuthorizedClient buildOAuthClient(UserConnectorConfig config) {
        ClientRegistration clientRegistration = clientRegistrationRepository.
                findByRegistrationId(config.getType().getConnectorType());

        long accessTokenExpiresAt = config.getConnectorConfig()
                .get(ConnectorConfigConstants.OAuth.ACCESS_TOKEN_EXPIRES_AT).asLong();

        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                config.getConnectorConfig().get(ConnectorConfigConstants.OAuth.ACCESS_TOKEN).asText(),
                null,
                Instant.ofEpochSecond(accessTokenExpiresAt),
                clientRegistration.getScopes());


        return new OAuth2AuthorizedClient(
                clientRegistration,
                config.getUser().getUid(),
                accessToken
        );
    }
}
