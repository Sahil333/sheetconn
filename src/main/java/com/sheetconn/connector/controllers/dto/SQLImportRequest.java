package com.sheetconn.connector.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SQLImportRequest {

    String workbookId;
    String sheetId;
    String provider;
    String connectorId;
    String query;
}
