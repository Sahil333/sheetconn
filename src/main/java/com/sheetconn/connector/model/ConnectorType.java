package com.sheetconn.connector.model;

public enum ConnectorType {
    
    POSTGRESQL("postgresql"),
    GOOGLE_SHEETS("google-sheets");

    private String connectorType;

    ConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    public String getConnectorType() {
        return connectorType;
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
