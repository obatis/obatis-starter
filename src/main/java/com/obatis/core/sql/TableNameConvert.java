package com.obatis.core.sql;

public class TableNameConvert {

	protected static final String getTableAsName(String tableName) {
		return tableName.replace("_", "");
	}
	
}
