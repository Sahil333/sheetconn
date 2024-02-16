package com.sheetconn.connector.controllers.dto;

import com.sheetconn.connector.model.ConnectorType;
import lombok.Getter;

@Getter
public enum SheetProvider {

    GOOGLE_SHEET(ConnectorType.GOOGLE_SHEETS);

    private final ConnectorType connectorType;
    SheetProvider(ConnectorType type) {
        this.connectorType = type;
    }
}
