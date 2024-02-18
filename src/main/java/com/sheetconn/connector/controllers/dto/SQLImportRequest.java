package com.sheetconn.connector.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SQLImportRequest {

    String workbookId;
    String sheetId;
    SheetProvider provider;
    String connectorId;
    String query;
    String range;
}
