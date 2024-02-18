package com.sheetconn.connector.sheet.google;

public abstract class TypeConverter {

    private TypeConverter next;

    public Object convert(Object val) {
        if(next != null) {
            return next.convert(val);
        } else {
            throw new RuntimeException("Could not convert type : " + val.getClass() + ". Please implement a converter for this");
        }
    }

    public void setNext(TypeConverter converter) {
        this.next = converter;
    }
}
