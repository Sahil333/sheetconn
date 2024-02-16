package com.sheetconn.connector.controllers;

import com.sheetconn.connector.oauth.ApplicationAuthenticationToken;
import com.sheetconn.connector.service.SpreadsheetWriterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationCodeGrantFilter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sheetconn.connector.connectors.postgres.model.QueryResult;
import com.sheetconn.connector.controllers.dto.SQLImportRequest;
import com.sheetconn.connector.service.SQLConnectorService;

@RestController
@RequestMapping("v1/sql")
@Slf4j
public class SQLConnectorController {
    
    private SQLConnectorService sqlConnector;
    private SpreadsheetWriterService writerService;

    public SQLConnectorController(SQLConnectorService sqlConnector) {
        this.sqlConnector = sqlConnector;
    }

    @PostMapping("postgres/import")
    public void importPostgreSql(@RequestBody SQLImportRequest sqlQueryRequest) {

        QueryResult data = sqlConnector.fetchPostgresDataUsingQuery(
            sqlQueryRequest.getConnectorId(), sqlQueryRequest.getQuery());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("Running write for user : {}", auth.getPrincipal());

//        writerService.write(auth.getPrincipal(),
//                sqlQueryRequest.getProvider(),
//                data,
//                sqlQueryRequest.getWorkbookId(),
//                sqlQueryRequest.getSheetId(),
//                sqlQueryRequest.getRange());
    }
}
