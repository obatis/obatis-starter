package com.sbatis.core.sql;

import com.sbatis.core.exception.HandleException;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * sql 语句构建代理类，作用于分发代理构建，针对特殊的sql 语句，根据数据库加载信息选择数据库类型
 * @author HuangLongPu
 */
public class SqlHandleProvider<T> {

	protected static AbstractSqlHandleMethod method;
	protected static AbstractInsertMethod insertMethod;

	private SqlHandleProvider() {

	}

	public static String getInsertSql(Object obj, Class<?> cls, String tableName) throws HandleException {
		return insertMethod.getInsertSql(obj, cls, tableName);
	}

	public static String getInsertBatchSql(List<?> obj, Class<?> cls, String tableName) throws HandleException {
		return insertMethod.handleInsertBatchSql(obj, cls, tableName);
	}

	public static String getUpdateSql(Map<String, Object> param, String tableName) throws HandleException {
		return method.getUpdateSql(param, tableName);
	}

	public static String getUpdateBatchSql(Map<String, Object> param, String tableName) throws HandleException {
		return method.getUpdateBatchSql(param, tableName);
	}

	public static String getDeleteByIdSql(String tableName) throws HandleException {
		return method.getDeleteByIdSql(tableName);
	}

	public static String getDeleteSql(Map<String, Object> param, String tableName) throws HandleException {
		return method.getDeleteSql(param, tableName);
	}

	public static String getSelectByIdSql(String[] columns, BigInteger id, String tableName) {
		return method.getSelectByIdSql(columns, id, tableName);
	}

	/**
	 * 根据map，拼接SQL
	 * @author HuangLongPu
	 * @param param
	 * @param tableName
	 * @return
	 * @throws HandleException
	 */
	public static String getSelectSql(Map<String, Object> param, String tableName) throws HandleException {
		return method.getSelectSql(param, tableName);
	}

	public static String getValidateSql(Map<String, Object> param, String tableName) throws HandleException {
		return method.getValidateSql(param, tableName);
	}

	public static void getQueryPageSql(Map<String, Object> param, String tableName) {
		method.getQueryPageSql(param, tableName);
	}

	public static String getReplaceSql(String sql, int index) {

		return method.getReplaceSql(sql, index);
	}

	public static String appendPageSql(String sql, int indexPage, int pageSize, boolean reset) {
		return method.appendPageSql(sql, indexPage, pageSize, reset);
	}
}
