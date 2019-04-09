package com.sbatis.core.mapper;

import com.sbatis.core.sql.BaseSqlProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;
import java.util.Map;

public interface BaseResultSessionMapper<R> {

	@SelectProvider(type = BaseSqlProvider.class, method = "find")
	R findR(@Param("request") Map<String, Object> param, String tableName);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "find")
	List<R> listR(@Param("request") Map<String, Object> param, String tableName);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "replaceSql")
	R getBySqlR(String sql, @Param("request") List<Object> param);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "replaceSql")
	List<R> listBySqlR(String sql, @Param("request") List<Object> param);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "replaceSql")
	List<Map<String, Object>> listMapBySqlR(String sql, @Param("request") List<Object> param);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "pageSql")
	List<R> pageR(String sql, @Param("request") Map<String, Object> param);
}
