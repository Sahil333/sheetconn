package com.sheetconn.connector.model;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@AllArgsConstructor
@Builder
public class UserConnectorConfig {

    @Id
    private String id;
    
    @Column(name = "uid")
    private String uid;

    @Column(name = "type")
    private ConnectorType type;
    
    @Column(name = "connector_config")
    private JsonNode connectorConfig;
}
