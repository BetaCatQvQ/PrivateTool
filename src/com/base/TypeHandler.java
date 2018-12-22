package com.base;

import java.util.Date;

public class TypeHandler {
	public static Class<?> getClassByHandler(String dataType) {
		Class<?> claz = null;
		switch (dataType) {
		case "varchar":
			claz = String.class;
			break;
		case "float":
			claz = Double.class;
			break;
		case "int":
			claz = Integer.class;
			break;
		case "timestamp":
			claz = Date.class;
			break;
		}
		return claz;
	}
}
