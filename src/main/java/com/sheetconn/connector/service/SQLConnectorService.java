package com.sheetconn.connector.service;

import org.springframework.stereotype.Service;

import com.sheetconn.connector.connectors.postgres.PostgresConnector;
import com.sheetconn.connector.connectors.postgres.model.QueryResult;
import com.sheetconn.connector.model.ConnectorType;
import com.sheetconn.connector.model.UserConnectorConfig;

@Service
public class SQLConnectorService {
 
    private ConnectorRegistryService connectorRegistry;

    public SQLConnectorService(ConnectorRegistryService connectorRegistry) {
        this.connectorRegistry = connectorRegistry;
    }

    public QueryResult fetchPostgresDataUsingQuery(String connectorId, String query) {
        UserConnectorConfig config = connectorRegistry.getConfig(connectorId);

        if(config.getType() != ConnectorType.POSTGRESQL) {
            throw new RuntimeException("Config type mismatch, required : POSTGRESQL got :" + config.getType());
        }

        try (PostgresConnector connector = new PostgresConnector(
            config.getConnectorConfig().get("host").asText(),
            config.getConnectorConfig().get("port").asInt(),
            config.getConnectorConfig().get("database").asText(),
            config.getConnectorConfig().get("user").asText(),
            config.getConnectorConfig().get("password").asText()
        )) {
            return connector.runSimpleQuery(query);
        }
    }
}
