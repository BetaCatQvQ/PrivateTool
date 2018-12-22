package com.base;

/**
 * @author woshi
 */
public class DBRecord {
	private String tableName;
	private String columnName;
	private String dataType;
	private Class<?> propertyClass;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public Class<?> getPropertyClass() {
		return propertyClass;
	}

	public void setPropertyClass(Class<?> propertyClass) {
		this.propertyClass = propertyClass;
	}

}
