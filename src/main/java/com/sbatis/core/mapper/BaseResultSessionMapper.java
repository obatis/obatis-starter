package com.sbatis.core.mapper;

import com.sbatis.core.sql.SqlProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;
import java.util.Map;

public interface BaseResultSessionMapper<R> {

	@SelectProvider(type = SqlProvider.class, method = "find")
	R findR(@Param("request") Map<String, Object> param, String tableName);
	
	@SelectProvider(type = SqlProvider.class, method = "find")
	List<R> listR(@Param("request") Map<String, Object> param, String tableName);
	
	@SelectProvider(type = SqlProvider.class, method = "replaceSql")
	R findBySqlR(String sql, @Param("request") List<Object> list);
	
	@SelectProvider(type = SqlProvider.class, method = "replaceSql")
	List<R> listBySqlR(String sql, @Param("request") List<Object> list);
	
	@SelectProvider(type = SqlProvider.class, method = "replaceSql")
	List<Map<String, Object>> listMapBySqlR(String sql, @Param("request") List<Object> list);
	
	@SelectProvider(type = SqlProvider.class, method = "pageSql")
	List<R> pageR(String sql, @Param("request") Map<String, Object> params);
}
