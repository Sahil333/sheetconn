package com.sheetconn.connector.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sheetconn.connector.connectors.postgres.model.QueryResult;
import com.sheetconn.connector.controllers.dto.SQLImportRequest;
import com.sheetconn.connector.service.SQLConnectorService;

@RestController
@RequestMapping("v1/sql")
public class SQLConnectorController {
    
    private SQLConnectorService sqlConnector;

    public SQLConnectorController(SQLConnectorService sqlConnector) {
        this.sqlConnector = sqlConnector;
    }

    @PostMapping("postgres/import")
    public void runPostgreSql(@RequestBody SQLImportRequest sqlQueryRequest) {
        QueryResult data = sqlConnector.fetchPostgresDataUsingQuery(
            sqlQueryRequest.getConnectorId(), sqlQueryRequest.getQuery());
        
        
    }
}
