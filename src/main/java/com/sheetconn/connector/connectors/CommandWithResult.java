package com.sheetconn.connector.connectors;

public abstract class CommandWithResult<T> implements Command {

    T result;
    public T getResult() {
        return result;
    }
}
