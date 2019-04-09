package com.sbatis.core.mapper;

import com.sbatis.core.BaseCommonEntity;
import com.sbatis.core.sql.BaseSqlProvider;
import org.apache.ibatis.annotations.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * mapper基类
 */
public interface BaseBeanMapper<T extends BaseCommonEntity> {
	
	@InsertProvider(type = BaseSqlProvider.class, method = "insert")
	int insert(@Param("param") T t, String tableName, Class<T> cls);
	
	@InsertProvider(type = BaseSqlProvider.class, method = "insertBatch")
	int insertBatch(@Param("param") List<T> list, String tableName, Class<T> cls);
	
	@UpdateProvider(type = BaseSqlProvider.class, method = "update")
	int update(@Param("param") Map<String, Object> param, String tableName);
	
	@UpdateProvider(type = BaseSqlProvider.class, method = "updateBatch")
	int updateBatch(@Param("param") Map<String, Object> param, String tableName);
	
	@DeleteProvider(type = BaseSqlProvider.class, method = "deleteById")
	int deleteById(@Param("id") BigInteger id, String tableName);
	
	@DeleteProvider(type = BaseSqlProvider.class, method = "delete")
	int delete(@Param("param") Map<String, Object> param, String tableName);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "find")
	T find(@Param("param") Map<String, Object> param, String tableName);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "validate")
	int validate(@Param("param") Map<String, Object> param, String tableName);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "find")
	Map<String, Object> findToMap(@Param("param") Map<String, Object> param, String tableName);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "find")
	Object getObject(@Param("param") Map<String, Object> param, String tableName);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "replaceSql")
	Object getObjectBySql(String sql, @Param("param") List<Object> param);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "replaceSql")
	T getBySql(String sql, @Param("param") List<Object> param);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "replaceSql")
	Map<String, Object> getMapBySql(String sql, @Param("param") List<Object> param);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "find")
	List<T> list(@Param("param") Map<String, Object> param, String tableName);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "find")
	List<Map<String, Object>> query(@Param("param") Map<String, Object> param, String tableName);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "replaceSql")
	List<T> listBySql(String sql, @Param("param") List<Object> param);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "replaceSql")
	List<Map<String, Object>> listMapBySql(String sql, @Param("param") List<Object> param);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "returnParamSql")
	int getTotal(String sql, @Param("param") Map<String, Object> param);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "replaceSql")
	int getTotalByParam(String sql, @Param("param") List<Object> param);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "pageSql")
	List<T> page(String sql, @Param("param") Map<String, Object> param);
}
