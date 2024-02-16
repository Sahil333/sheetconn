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
            config.getConnectorConfig().get(ConnectorConfigConstants.Postgres.HOST).asText(),
            config.getConnectorConfig().get(ConnectorConfigConstants.Postgres.PORT).asInt(),
            config.getConnectorConfig().get(ConnectorConfigConstants.Postgres.DATABASE).asText(),
            config.getConnectorConfig().get(ConnectorConfigConstants.Postgres.USER).asText(),
            config.getConnectorConfig().get(ConnectorConfigConstants.Postgres.PASSWORD).asText()
        )) {
            return connector.runSimpleQuery(query);
        }
    }
}
