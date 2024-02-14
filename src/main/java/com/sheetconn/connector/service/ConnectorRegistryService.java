package com.sheetconn.connector.service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sheetconn.connector.controllers.dto.PostgresConfig;
import com.sheetconn.connector.model.ConnectorType;
import com.sheetconn.connector.model.UserConnectorConfig;
import com.sheetconn.connector.oauth.OAuth2AuthenticationToken;
import com.sheetconn.connector.repository.UserConnectorConfigRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ConnectorRegistryService {
    
    private final UserConnectorConfigRepository connectorConfigRepository;

    public ConnectorRegistryService(UserConnectorConfigRepository connectorConfigRepository) {
        this.connectorConfigRepository = connectorConfigRepository;
    }

    public void registerPostgresConnector(PostgresConfig postgresConfig, String uid) {
        ObjectNode connectorConfig = JsonNodeFactory.instance.objectNode();
        connectorConfig.put("host", postgresConfig.getHost());
        connectorConfig.put("port", postgresConfig.getPort());
        connectorConfig.put("database", postgresConfig.getDatabase());
        connectorConfig.put("user", postgresConfig.getUser());
        connectorConfig.put("password", postgresConfig.getPassword());

        UserConnectorConfig config = UserConnectorConfig.builder()
            .connectorConfig(connectorConfig)
            .type(ConnectorType.POSTGRESQL)
            .uid(uid)
            .id(UUID.randomUUID().toString())
            .build();

        connectorConfigRepository.save(config);
    }

    public UserConnectorConfig getConfig(String connectorId) {
        return connectorConfigRepository.findById(connectorId)
            .orElseThrow(() -> {
                return new RuntimeException("Config not found for connectorId: " + connectorId);
            });
    }

    public List<UserConnectorConfig> getConfigs(String uid) {
        return connectorConfigRepository.findAllByUid(uid);
    }

    public void registerOAuthConnect(String uid, OAuth2AuthenticationToken authenticationToken) {
        ObjectNode connectorConfig = JsonNodeFactory.instance.objectNode();
        connectorConfig.put("clientRegistrationId", authenticationToken.getClientRegistration().getClientId());
        connectorConfig.put("subject", authenticationToken.getPrincipal().toString());
        connectorConfig.put("access_token", authenticationToken.getAccessToken().getTokenValue());
        connectorConfig.put("access_token_expires_at", authenticationToken.getAccessToken().getExpiresAt().getEpochSecond());
        connectorConfig.put("refresh_token", authenticationToken.getRefreshToken().getTokenValue());
        connectorConfig.put("refresh_token_expires_at", authenticationToken.getRefreshToken().getExpiresAt().getEpochSecond());
        UserConnectorConfig config = UserConnectorConfig.builder()
                .uid(uid)
                .id(UUID.randomUUID().toString())
                .type(ConnectorType.getConnectorTypeFromName(authenticationToken.getClientRegistration().getClientName()))
                .connectorConfig(connectorConfig)
                .build();

        connectorConfigRepository.save(config);
    }
}
