package com.obatis.core.sql;

import com.obatis.core.exception.HandleException;
import com.obatis.core.sql.mysql.HandleInsertBatchMethod;
import com.obatis.core.sql.mysql.MysqlCommonMethod;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * sql 语句构建代理类，作用于分发代理构建，针对特殊的sql 语句，根据数据库加载信息选择数据库类型
 * @author HuangLongPu
 */
public class SqlHandleProvider {

	protected static AbstractSqlHandleMethod sqlHandleMethod = new MysqlCommonMethod();
	protected static AbstractInsertMethod insertMethod = new HandleInsertBatchMethod();

	private SqlHandleProvider() {

	}

	/**
	 * 获取insert sql 语句
	 * @param obj
	 * @param cls
	 * @param tableName
	 * @return
	 * @throws HandleException
	 */
	public static String getInsertSql(Object obj, Class<?> cls, String tableName) throws HandleException {
		return insertMethod.handleInsertSql(obj, cls, tableName);
	}

	/**
	 * 获取批量添加 insert sql 语句
	 * @param obj
	 * @param cls
	 * @param tableName
	 * @return
	 * @throws HandleException
	 */
	public static String getBatchInsertSql(List<?> obj, Class<?> cls, String tableName) throws HandleException {
		return insertMethod.handleBatchInsertSql(obj, cls, tableName);
	}

	/**
	 * 获取更新 update sql 语句
	 * @param providers
	 * @param tableName
	 * @return
	 * @throws HandleException
	 */
	public static String getUpdateSql(Map<String, Object> providers, String tableName) throws HandleException {
		return sqlHandleMethod.getUpdateSql(providers, tableName);
	}

	/**
	 * 获取批量更新 update sql 语句
	 * @param providers
	 * @param tableName
	 * @return
	 * @throws HandleException
	 */
	public static String getBatchUpdateSql(Map<String, Object> providers, String tableName) throws HandleException {
		return sqlHandleMethod.getUpdateBatchSql(providers, tableName);
	}

	/**
	 * 获取根据 id 进行删除的 delete sql 语句，例如 delete from tableName where id = ？
	 * @param tableName
	 * @return
	 * @throws HandleException
	 */
	public static String getDeleteByIdSql(String tableName) throws HandleException {
		return sqlHandleMethod.getDeleteByIdSql(tableName);
	}

	/**
	 * 获取常规删除的 delete sql 语句
	 * @param providers
	 * @param tableName
	 * @return
	 * @throws HandleException
	 */
	public static String getDeleteSql(Map<String, Object> providers, String tableName) throws HandleException {
		return sqlHandleMethod.getDeleteSql(providers, tableName);
	}

	/**
	 * 获取根据ID查询的 select sql 语句，例如 select column from  tableName where id = ?
	 * @param columns
	 * @param id
	 * @param tableName
	 * @return
	 */
	public static String getSelectByIdSql(String[] columns, BigInteger id, String tableName) {
		return sqlHandleMethod.getSelectByIdSql(columns, id, tableName);
	}

	/**
	 * 根据map，拼接SQL
	 * @param providers
	 * @param tableName
	 * @return
	 * @throws HandleException
	 */
	public static String getSelectSql(Map<String, Object> providers, String tableName) throws HandleException {
		return sqlHandleMethod.getSelectSql(providers, tableName);
	}

	public static String getSelectTopSql(Map<String, Object> providers, int top, String tableName) throws HandleException {
		return sqlHandleMethod.getSelectSql(providers, tableName) + " limit " + top;
	}

	/**
	 * 获取校验的 sql 语句，原理为根据查询条件，得到count计数的值，映射 sql 为 select count(*) from tableName where filterName = ?
	 * @param providers
	 * @param tableName
	 * @return
	 * @throws HandleException
	 */
	public static String getValidateSql(Map<String, Object> providers, String tableName) throws HandleException {
		return sqlHandleMethod.getValidateSql(providers, tableName);
	}

	/**
	 * 获取分页查询的 sql 语句，总共包含两条 sql 语句，一条为查询数据，一条为求总条数，sql 存放于 map 中
	 * @param providers
	 * @param tableName
	 */
	public static void getQueryPageSql(Map<String, Object> providers, String tableName) {
		sqlHandleMethod.getQueryPageSql(providers, tableName);
	}

	/**
	 * 替换 sql 语句，作用于程序里拼接的复杂 sql，将 filterName = ？格式转换为支持mybatis的格式
	 * @param sql
	 * @param index
	 * @return
	 */
	public static String getReplaceSql(String sql, int index) {
		return sqlHandleMethod.getReplaceSql(sql, index);
	}

	/**
	 * 作用于分页时拼接分页信息
	 * @param sql
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 */
	public static String appendPageSql(String sql, int pageNumber, int pageSize) {
		return sqlHandleMethod.appendPageSql(sql, pageNumber, pageSize);
	}
}
