package com.sbatis.core.mapper;

import com.sbatis.core.BaseCommonEntity;
import com.sbatis.core.sql.BaseSqlProvider;
import org.apache.ibatis.annotations.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * mapper的顶层父类，每一个实体对应的map都需要继承，提供类常规的对数据库的操作
 */
public interface BaseBeanSessionMapper<T extends BaseCommonEntity> {
	
	@InsertProvider(type = BaseSqlProvider.class, method = "insert")
	int insert(@Param("request") T t, String tableName, Class<T> cls);
	
	@InsertProvider(type = BaseSqlProvider.class, method = "insertBatch")
	int insertBatch(@Param("request") List<T> list, String tableName, Class<T> cls);
	
	@UpdateProvider(type = BaseSqlProvider.class, method = "update")
	int update(@Param("request") Map<String, Object> param, String tableName);
	
	@UpdateProvider(type = BaseSqlProvider.class, method = "updateBatch")
	int updateBatch(@Param("request") Map<String, Object> param, String tableName);
	
	@DeleteProvider(type = BaseSqlProvider.class, method = "deleteById")
	int deleteById(@Param("id") BigInteger id, String tableName);
	
	@DeleteProvider(type = BaseSqlProvider.class, method = "delete")
	int delete(@Param("request") Map<String, Object> param, String tableName);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "find")
	T find(@Param("request") Map<String, Object> param, String tableName);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "validate")
	int validate(@Param("request") Map<String, Object> param, String tableName);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "find")
	Map<String, Object> findToMap(@Param("request") Map<String, Object> param, String tableName);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "find")
	Object getObject(@Param("request") Map<String, Object> param, String tableName);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "replaceSql")
	Object getObjectBySql(String sql, @Param("request") List<Object> param);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "replaceSql")
	T getBySql(String sql, @Param("request") List<Object> param);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "replaceSql")
	Map<String, Object> getMapBySql(String sql, @Param("request") List<Object> param);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "find")
	List<T> list(@Param("request") Map<String, Object> param, String tableName);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "find")
	List<Map<String, Object>> query(@Param("request") Map<String, Object> param, String tableName);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "replaceSql")
	List<T> listBySql(String sql, @Param("request") List<Object> param);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "replaceSql")
	List<Map<String, Object>> listMapBySql(String sql, @Param("request") List<Object> param);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "returnParamSql")
	int getTotal(String sql, @Param("request") Map<String, Object> param);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "replaceSql")
	int getTotalByParam(String sql, @Param("request") List<Object> param);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "pageSql")
	List<T> page(String sql, @Param("request") Map<String, Object> param);
}
