package com.obatis.core.mapper;

import com.obatis.core.CommonModel;
import com.obatis.core.sql.SqlProvider;
import org.apache.ibatis.annotations.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * mapper的顶层父类，每一个实体对应的map都需要继承，提供类常规的对数据库的操作
 */
public interface BaseBeanSessionMapper<T extends CommonModel> extends CommonMapper<T> {
	
	@InsertProvider(type = SqlProvider.class, method = "insert")
	int insert(@Param("request") T t, String tableName, Class<T> cls);
	
	@InsertProvider(type = SqlProvider.class, method = "batchInsert")
	int insertBatch(@Param("request") List<T> list, String tableName, Class<T> cls);
	
	@UpdateProvider(type = SqlProvider.class, method = "update")
	int update(@Param("request") Map<String, Object> params, String tableName);
	
	@UpdateProvider(type = SqlProvider.class, method = "batchUpdate")
	int updateBatch(@Param("request") Map<String, Object> params, String tableName);
	
	@DeleteProvider(type = SqlProvider.class, method = "deleteById")
	int deleteById(@Param("id") BigInteger id, String tableName);
	
	@DeleteProvider(type = SqlProvider.class, method = "delete")
	int delete(@Param("request") Map<String, Object> param, String tableName);

	@SelectProvider(type = SqlProvider.class, method = "validate")
	int validate(@Param("request") Map<String, Object> param, String tableName);
	
	@SelectProvider(type = SqlProvider.class, method = "find")
	Map<String, Object> findToMap(@Param("request") Map<String, Object> params, String tableName);
	
	@SelectProvider(type = SqlProvider.class, method = "find")
	Object findObject(@Param("request") Map<String, Object> param, String tableName);
	
	@SelectProvider(type = SqlProvider.class, method = "replaceSql")
	Object findObjectBySql(String sql, @Param("request") List<Object> list);

	@SelectProvider(type = SqlProvider.class, method = "replaceSql")
	Map<String, Object> findMapBySql(String sql, @Param("request") List<Object> list);

	@SelectProvider(type = SqlProvider.class, method = "find")
	List<Map<String, Object>> query(@Param("request") Map<String, Object> params, String tableName);

	@SelectProvider(type = SqlProvider.class, method = "returnParamSql")
	int findTotal(String sql, @Param("request") Map<String, Object> params);
	
	@SelectProvider(type = SqlProvider.class, method = "replaceSql")
	int findTotalByParam(String sql, @Param("request") List<Object> params);
}
