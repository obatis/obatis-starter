package com.obatis.core.sql.mysql;


import com.obatis.core.sql.AbstractSqlHandleMethod;

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
	protected String appendPageSql(String sql, int pageNumber, int pageSize) {
		return " limit " + getPageLimit(pageNumber, pageSize) + "," + pageSize;
	}

	@Override
	protected String getBatchUpdateDbSql(String sql) {
		return sql;
	}
	
	
}
