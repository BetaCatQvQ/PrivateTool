package com.base;

import java.util.List;

public class DBEntity {
    private String className;
    private List<DBRecord> columns;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<DBRecord> getColumns() {
        return columns;
    }

    public void setColumns(List<DBRecord> columns) {
        this.columns = columns;
    }

}
