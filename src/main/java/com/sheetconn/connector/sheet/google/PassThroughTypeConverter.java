package com.sheetconn.connector.sheet.google;

import java.util.Set;

public class PassThroughTypeConverter extends TypeConverter {

    Set<Class<?>> passThroughTypes =
            Set.of(
                    String.class,
                    Integer.class,
                    Long.class,
                    Boolean.class,
                    Short.class,
                    Double.class,
                    Float.class
                    );

    @Override
    public Object convert(Object val) {
        if(passThroughTypes.contains(val.getClass())) {
            return val;
        } else {
            return super.convert(val);
        }
    }
}
