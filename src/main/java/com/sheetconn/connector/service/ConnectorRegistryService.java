package com.sheetconn.connector.service;

import java.util.UUID;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sheetconn.connector.controllers.dto.PostgresConfig;
import com.sheetconn.connector.model.ConnectorType;
import com.sheetconn.connector.model.UserConnectorConfig;
import com.sheetconn.connector.repository.UserConnectorConfigRepository;

@Service
public class ConnectorRegistryService {
    
    private UserConnectorConfigRepository connectorConfigRepository;

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
}
