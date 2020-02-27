package com.obatis.core.sql;

import com.obatis.core.exception.HandleException;
import com.obatis.core.constant.SqlConstant;
import org.apache.ibatis.annotations.Param;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * 基础sql提供类
 * @author HuangLongPu
 */
public class SqlProvider<T> {
	
	public SqlProvider() {}
	
	public String insert(@Param("request") T t, String tableName, Class<T> cls) throws HandleException {
		return SqlHandleProvider.getInsertSql(t, cls, tableName);
	}
	
	public String batchInsert(@Param("request") List<T> list, String tableName, Class<T> cls) throws HandleException {
		return SqlHandleProvider.getBatchInsertSql(list, cls, tableName);
	}
	
	public String update(@Param("request") Map<String, Object> providers, String tableName) throws HandleException {
		return SqlHandleProvider.getUpdateSql(providers, tableName);
	}
	
	public String batchUpdate(@Param("request") Map<String, Object> providers, String tableName) throws HandleException {
		return SqlHandleProvider.getBatchUpdateSql(providers, tableName);
	}
	
	public String deleteById(@Param("id") BigInteger id, String tableName) throws HandleException {
		return SqlHandleProvider.getDeleteByIdSql(tableName);
	}
	
	public String delete(@Param("request") Map<String, Object> providers, String tableName) throws HandleException {
		return SqlHandleProvider.getDeleteSql(providers, tableName);
	}
	
	public String find(@Param("request") Map<String, Object> providers, String tableName) throws HandleException {
		return SqlHandleProvider.getSelectSql(providers, 0, tableName);
	}

	public String findLimit(@Param("request") Map<String, Object> providers, int limit, String tableName) throws HandleException {
		return SqlHandleProvider.getSelectSql(providers, limit, tableName);
	}

	public String findOne(@Param("request") Map<String, Object> providers, String tableName) throws HandleException {
		return SqlHandleProvider.getSelectSql(providers, 1, tableName);
	}

	public String list(@Param("request") Map<String, Object> providers, int limit, String tableName) throws HandleException {
		return SqlHandleProvider.getSelectSql(providers, limit, tableName);
	}
	
	public String validate(@Param("request") Map<String, Object> providers, String tableName) throws HandleException {
		return SqlHandleProvider.getValidateSql(providers, tableName);
	}
	
	public String replaceSql(String sql, @Param("request") List<Object> params) {
		int index = 0;
		return SqlHandleProvider.getReplaceSql(sql, index);
	}

	public String returnParamSql(String sql, @Param("request") Map<String, Object> providers) {
		return sql;
	}
	
	public String pageSql(String sql, @Param("request") Map<String, Object> providers) {
		QueryProvider queryProvider = (QueryProvider) providers.get(SqlConstant.PROVIDER_OBJ);
//		if (PageEnum.IS_PAGE_TRUE.equals(queryProvider.getIsPage())) {
			return SqlHandleProvider.appendPageSql(sql, queryProvider.getPageNumber(), queryProvider.getPageSize());
//		}
//		return sql;
	}
}
