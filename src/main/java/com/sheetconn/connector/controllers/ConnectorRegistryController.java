package com.sheetconn.connector.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sheetconn.connector.controllers.dto.PostgresConfig;
import com.sheetconn.connector.model.UserConnectorConfig;
import com.sheetconn.connector.service.ConnectorRegistryService;

@RestController
@RequestMapping("v1/connector/")
public class ConnectorRegistryController {
    
    private final ConnectorRegistryService connectorRegistry;

    public ConnectorRegistryController(ConnectorRegistryService connectorRegistry) {
        this.connectorRegistry = connectorRegistry;
    }

    @PostMapping
    public void addPostgresConnector(@RequestBody PostgresConfig postgresConfig) {
        connectorRegistry.registerPostgresConnector(postgresConfig, "1");
    }

    @GetMapping
    public List<UserConnectorConfig> getConnectors() {
        return connectorRegistry.getConfigs("1");
    }
}
