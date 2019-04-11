package com.sbatis.core.sql.mysql;


import com.sbatis.core.sql.AbstractSqlHandleMethod;

/**
 * @author HuangLongPu
 */
public class MysqlCommonMethod extends AbstractSqlHandleMethod {

	public MysqlCommonMethod() {
		super();
	}

	@Override
	protected String getLikeSql(String expression) {
		return "\"%\"" + expression + "\"%\"";
	}

	@Override
	protected String getLeftLikeSql(String expression) {
		return expression + "\"%\"";
	}

	@Override
	protected String getRightLikeSql(String expression) {
		return "\"%\"" + expression;
	}

	@Override
	protected String appendPageSql(String sql, int pageNumber, int pageSize, boolean reset) {
		if (reset) {
			sql += " limit 0," + pageSize;
		} else {
			sql += " limit " + getPageLimit(pageNumber, pageSize) + "," + pageSize;
		}
		return sql;
	}

	@Override
	protected String getBatchUpdateDbSql(String sql) {
		return sql;
	}
	
	
}
