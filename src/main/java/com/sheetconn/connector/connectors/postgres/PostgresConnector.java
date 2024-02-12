package com.sheetconn.connector.connectors.postgres;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.sheetconn.connector.connectors.postgres.expceptions.ConnectTimeoutException;
import com.sheetconn.connector.connectors.postgres.model.Column;
import com.sheetconn.connector.connectors.postgres.model.QueryResult;
import com.sheetconn.connector.connectors.postgres.model.SchemaResult;
import com.sheetconn.connector.connectors.postgres.model.Table;

public class PostgresConnector implements AutoCloseable {

    public static final Integer ROW_LIMIT = 200000;
    private Connection connection;

    public PostgresConnector(String host, Integer port, String database, String user, String pwd) {
        connect(host, port, database, user, pwd);
    }

    private void connect(String host, Integer port, String database, String user, String pwd) {
        try {
            DriverManager.setLoginTimeout(10);
            connection = DriverManager.getConnection(
                buildUrl(host, port, database), user, pwd);
        } catch (SQLTimeoutException e) {
            throw new ConnectTimeoutException();
        } catch (SQLException e) {
            
        }
    }

    private String buildUrl(String host, Integer port, String database) {
        return "jdbc:postgresql://"
            + host
            + ":"
            + port.toString()
            + "/"
            + database
            + "&sslmode=verify-full&sslfactory=org.postgresql.ssl.DefaultJavaSSLFactory";
    }

    public SchemaResult fetchSchema() {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            SchemaResult result = new SchemaResult();
            result.setTables(new ArrayList<>());
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                Table table = new Table();
                table.setName(tableName);
                System.out.println("Table: " + tableName);

                ResultSet columns = metaData.getColumns(null, null, tableName, null);
                List<Column> columnsResult = new ArrayList<>();
                while (columns.next()) {
                    Column column = new Column();
                    String columnName = columns.getString("COLUMN_NAME");
                    int columnType = columns.getInt("DATA_TYPE");
                    column.setName(columnName);
                    column.setType(columnType);
                    columnsResult.add(column);
                }
                table.setColumns(columnsResult);
                result.getTables().add(table);
                columns.close();
            }
            tables.close();
            return result;
        } catch (SQLException e) {
           throw new RuntimeException(e);
        }
    }

    public QueryResult runSimpleQuery(String readQuery) {
        QueryResult result = new QueryResult();
        try {
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                // Start a read-only transaction
                statement.execute("START TRANSACTION READ ONLY;");
                
                statement.setFetchSize(5000);
                int count = 0;
                List<List<String>> data = new LinkedList<>();
                List<Column> columns = new ArrayList<>();
                try (ResultSet resultSet = statement.executeQuery(readQuery)) {
                    ResultSetMetaData metaData = resultSet.getMetaData();
                    for(int i=1; i<metaData.getColumnCount(); ++i) {
                        columns.add(new Column(metaData.getColumnName(i), metaData.getColumnType(i)));
                    }
                    while (resultSet.next() && count < ROW_LIMIT) {
                        ++count;
                        List<String> rowData = new ArrayList<>(metaData.getColumnCount());
                        for(int i=1; i<=metaData.getColumnCount(); ++i) {
                            rowData.set(i-1, resultSet.getString(i));
                        }
                        data.add(rowData);
                    }
                }

                result.setColumns(columns);
                result.setData(data);
            }
            connection.commit();
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            
        }
    }
}
