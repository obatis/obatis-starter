package com.sbatis.core.sql;

import com.sbatis.core.constant.SqlConstant;
import com.sbatis.core.constant.type.PageEnum;
import com.sbatis.core.exception.HandleException;
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
	
	public String update(@Param("request") Map<String, Object> param, String tableName) throws HandleException {
		return SqlHandleProvider.getUpdateSql(param, tableName);
	}
	
	public String batchUpdate(@Param("request") Map<String, Object> param, String tableName) throws HandleException {
		return SqlHandleProvider.getBatchUpdateSql(param, tableName);
	}
	
	public String deleteById(@Param("id") BigInteger id, String tableName) throws HandleException {
		return SqlHandleProvider.getDeleteByIdSql(tableName);
	}
	
	public String delete(@Param("request") Map<String, Object> param, String tableName) throws HandleException {
		return SqlHandleProvider.getDeleteSql(param, tableName);
	}
	
	public String find(@Param("request") Map<String, Object> param, String tableName) throws HandleException {
		return SqlHandleProvider.getSelectSql(param, tableName);
	}
	
	public String validate(@Param("request") Map<String, Object> param, String tableName) throws HandleException {
		return SqlHandleProvider.getValidateSql(param, tableName);
	}
	
	public String replaceSql(String sql, @Param("request") List<Object> object) {
		int index = 0;
		return SqlHandleProvider.getReplaceSql(sql, index);
	}

	public String returnParamSql(String sql, @Param("request") Map<String, Object> param) {
		return sql;
	}
	
	public String pageSql(String sql, @Param("request") Map<String, Object> param) {
		QueryProvider QueryProvider = (QueryProvider) param.get(SqlConstant.PROVIDER_OBJ);
		if (PageEnum.IS_PAGE_TRUE.equals(QueryProvider.getIsPage())) {
			return SqlHandleProvider.appendPageSql(sql, QueryProvider.getPageNumber(), QueryProvider.getPageSize(), QueryProvider.isResetIndexPage());
		}
		return sql;
	}
}
