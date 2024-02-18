package com.sheetconn.connector.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserConnectorConfig {

    @Id
    private String id;
    
    @ManyToOne
    @JoinColumn(name = "uid")
    @JsonIgnore
    private User user;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private ConnectorType type;
    
    @Column(name = "connector_config", columnDefinition = "jsonb")
    @Type(JsonType.class)
    private JsonNode connectorConfig;
}
