package com.sheetconn.connector.controllers;

import com.sheetconn.connector.connectors.postgres.model.Column;
import com.sheetconn.connector.expceptions.GoogleSheetWriteException;
import com.sheetconn.connector.service.SpreadsheetWriterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sheetconn.connector.connectors.postgres.model.QueryResult;
import com.sheetconn.connector.controllers.dto.SQLImportRequest;
import com.sheetconn.connector.service.SQLConnectorService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@RestController
@RequestMapping("v1/sql")
@Slf4j
public class SQLConnectorController {
    
    private final SQLConnectorService sqlConnector;
    private final SpreadsheetWriterService writerService;

    public SQLConnectorController(SQLConnectorService sqlConnector, SpreadsheetWriterService writerService) {
        this.sqlConnector = sqlConnector;
        this.writerService = writerService;
    }

    @PostMapping("postgres/import")
    public void importPostgreSql(@RequestBody SQLImportRequest sqlQueryRequest) {
        QueryResult result = sqlConnector.fetchPostgresDataUsingQuery(
            sqlQueryRequest.getConnectorId(), sqlQueryRequest.getQuery());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        log.info("Running write for user : {}", auth.getPrincipal());

        List<List<Object>> data = result.getData();

        List<Object> columnNames = new ArrayList<>(result.getColumns().stream().map(Column::getName).toList());

        // Using LinkedList for data
        data.add(0, columnNames);

        try {
            writerService.write(auth.getName(),
                    sqlQueryRequest.getProvider(),
                    sqlQueryRequest.getWorkbookId(),
                    sqlQueryRequest.getSheetId(),
                    sqlQueryRequest.getRange(),
                    data);
        } catch (IOException e) {
            throw new GoogleSheetWriteException("Failed to write to the google sheet : " + sqlQueryRequest.getWorkbookId(), e);
        }
    }
}
