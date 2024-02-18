package com.sheetconn.connector.connectors.postgres.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryResult {
    List<Column> columns;
    List<List<Object>> data;
}
