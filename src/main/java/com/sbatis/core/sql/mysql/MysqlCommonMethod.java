package com.sbatis.core.sql.mysql;


import com.sbatis.core.sql.AbstractMethod;

/**
 * @author HuangLongPu
 */
public class MysqlCommonMethod extends AbstractMethod {

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
	protected String appendPageSql(String sql, int pageNo, int pageSize, boolean reset) {
		if (reset) {
			sql += " limit 0," + pageSize;
		} else {
			sql += " limit " + getIndexPage(pageNo, pageSize) + "," + pageSize;
		}
		return sql;
	}

	@Override
	protected String getUpdateBatchDbSql(String sql) {
		return sql;
	}
	
	
}
