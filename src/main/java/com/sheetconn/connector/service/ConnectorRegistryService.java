package com.sheetconn.connector.service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sheetconn.connector.controllers.dto.PostgresConfig;
import com.sheetconn.connector.model.ConnectorType;
import com.sheetconn.connector.model.User;
import com.sheetconn.connector.model.UserConnectorConfig;
import com.sheetconn.connector.oauth.OAuth2AuthenticationToken;
import com.sheetconn.connector.repository.UserConnectorConfigRepository;
import com.sheetconn.connector.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ConnectorRegistryService {
    
    private final UserConnectorConfigRepository connectorConfigRepository;
    private final UserRepository userRepository;

    public ConnectorRegistryService(UserConnectorConfigRepository connectorConfigRepository,
                                    UserRepository userRepository) {
        this.connectorConfigRepository = connectorConfigRepository;
        this.userRepository = userRepository;
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
        connectorConfig.put("clientRegistrationId", authenticationToken.getClientRegistration().getClientId());
        connectorConfig.put("access_token", authenticationToken.getAccessToken().getTokenValue());
        connectorConfig.put("access_token_expires_at", authenticationToken.getAccessToken().getExpiresAt().getEpochSecond());
        connectorConfig.put("refresh_token", authenticationToken.getRefreshToken().getTokenValue());

        if (authenticationToken.getRefreshToken().getExpiresAt() == null) {
            connectorConfig.set("refresh_token_expires_at", NullNode.getInstance());
        } else {
            connectorConfig.put("refresh_token_expires_at", authenticationToken.getRefreshToken().getExpiresAt().getEpochSecond());
        }

        Claims claims = authenticationToken.getClaims();
        connectorConfig.put("sub", claims.getSubject());
        connectorConfig.put("email", claims.get("email", String.class));
        connectorConfig.put("name", claims.get("name", String.class));
        connectorConfig.put("picture", claims.get("picture", String.class));
        connectorConfig.put("phone_number", claims.get("phone_number", String.class));
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
            if(oauthConnector.getConnectorConfig().get("sub").asText().equals(subject)) {
                return oauthConnector;
            }
        }
        return null;
    }
}
