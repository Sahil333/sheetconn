package com.sheetconn.connector.model;

import lombok.Getter;

@Getter
public enum ConnectorType {
    
    POSTGRESQL("postgresql", false),
    GOOGLE_SHEETS("google-sheets", true);

    private String connectorType;
    private Boolean isOAuth;

    ConnectorType(String connectorType, Boolean isOAuth) {
        this.connectorType = connectorType;
        this.isOAuth = isOAuth;
    }

    public static ConnectorType getConnectorTypeFromName(String connectorName) {
        for(ConnectorType connectorType : ConnectorType.values()) {
            if(connectorType.getConnectorType().equals(connectorName)) {
                return connectorType;
            }
        }
        throw new RuntimeException("No ConnectorType found for : " + connectorName);
    }
}
