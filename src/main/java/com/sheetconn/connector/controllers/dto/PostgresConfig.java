package com.sheetconn.connector.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PostgresConfig {
    
    private String host;
    private Integer port;
    private String database;
    private String user;
    private String password;
}
