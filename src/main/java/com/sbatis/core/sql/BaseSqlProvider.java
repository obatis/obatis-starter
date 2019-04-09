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
 * @author admin
 */
public class BaseSqlProvider<T> {
	
	public BaseSqlProvider() {}
	
	public String insert(@Param("request") T t, String tableName, Class<T> cls) throws HandleException {
		return SqlProvider.getInsertSql(t, cls, tableName);
	}
	
	public String insertBatch(@Param("request") List<T> list, String tableName, Class<T> cls) throws HandleException {
		return SqlProvider.getInsertBatchSql(list, cls, tableName);
	}
	
	public String update(@Param("request") Map<String, Object> param, String tableName) throws HandleException {
		return SqlProvider.getUpdateSql(param, tableName);
	}
	
	public String updateBatch(@Param("request") Map<String, Object> param, String tableName) throws HandleException {
		return SqlProvider.getUpdateBatchSql(param, tableName);
	}
	
	public String deleteById(@Param("id") BigInteger id, String tableName) throws HandleException {
		return SqlProvider.getDeleteByIdSql(tableName);
	}
	
	public String delete(@Param("request") Map<String, Object> param, String tableName) throws HandleException {
		return SqlProvider.getDeleteSql(param, tableName);
	}
	
	public String find(@Param("request") Map<String, Object> param, String tableName) throws HandleException {
		return SqlProvider.getSelectSql(param, tableName);
	}
	
	public String validate(@Param("request") Map<String, Object> param, String tableName) throws HandleException {
		return SqlProvider.getValidateSql(param, tableName);
	}
	
	public String replaceSql(String sql, @Param("request") List<Object> object) {
		int index = 0;
		return SqlProvider.getReplaceSql(sql, index);
	}

	public String returnParamSql(String sql, @Param("request") Map<String, Object> param) {
		return sql;
	}
	
	public String pageSql(String sql, @Param("request") Map<String, Object> param) {
		QueryProvider QueryProvider = (QueryProvider) param.get(SqlConstant.PARAM_OBJ);
		if (PageEnum.IS_PAGE_TRUE.equals(QueryProvider.getIsPage())) {
			return SqlProvider.appendPageSql(sql, QueryProvider.getIndexPage(), QueryProvider.getPageSize(), QueryProvider.isResetIndexPage());
		}
		return sql;
	}
}
