package com.sheetconn.connector.sheet.google;

public class TypeConverterContainer extends TypeConverter {

    private final TypeConverter chain;

    public TypeConverterContainer() {
        chain = new PassThroughTypeConverter();
        chain.setNext(new JavaSqlTimeStampConverter());
    }

    @Override
    public Object convert(Object val) {
        return chain.convert(val);
    }
}
