package com.sheetconn.connector.model;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.Type;

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
    
    @Column(name = "connector_config", columnDefinition = "jsonb")
    @Type(JsonType.class)
    private JsonNode connectorConfig;
}
