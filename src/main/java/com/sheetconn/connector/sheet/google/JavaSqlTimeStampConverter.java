package com.sheetconn.connector.sheet.google;

import java.sql.Timestamp;

public class JavaSqlTimeStampConverter extends TypeConverter {

    @Override
    public Object convert(Object val) {
        if(val.getClass().equals(Timestamp.class)) {
            Timestamp time = (Timestamp) val;
            return time.toString();
        } else {
            return super.convert(val);
        }
    }
}
