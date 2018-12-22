package com.base;

import java.util.List;

public class DBEntity {
	private String className;
	private List<DBRecord> colums;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public List<DBRecord> getColums() {
		return colums;
	}

	public void setColums(List<DBRecord> colums) {
		this.colums = colums;
	}

}
