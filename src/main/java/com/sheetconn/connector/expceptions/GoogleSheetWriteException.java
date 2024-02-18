package com.sheetconn.connector.expceptions;

import java.io.IOException;

public class GoogleSheetWriteException extends RuntimeException {

    public GoogleSheetWriteException(String s, IOException e) {
        super(s, e);
    }
}
