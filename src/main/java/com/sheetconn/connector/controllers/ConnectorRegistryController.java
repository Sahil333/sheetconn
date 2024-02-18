package com.sheetconn.connector.controllers;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sheetconn.connector.controllers.dto.PostgresConfig;
import com.sheetconn.connector.model.UserConnectorConfig;
import com.sheetconn.connector.service.ConnectorRegistryService;

@RestController
@RequestMapping("v1/connector")
public class ConnectorRegistryController {
    
    private final ConnectorRegistryService connectorRegistry;

    public ConnectorRegistryController(ConnectorRegistryService connectorRegistry) {
        this.connectorRegistry = connectorRegistry;
    }

    @PostMapping
    public void addPostgresConnector(@RequestBody PostgresConfig postgresConfig) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        connectorRegistry.registerPostgresConnector(postgresConfig, auth.getName());
    }


    // TODO: only send back meta info on connectors, no sensitive credentials should be send back
    @GetMapping
    public List<UserConnectorConfig> getConnectors() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return connectorRegistry.getConfigs(auth.getName());
    }
}
