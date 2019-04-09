package com.sbatis.core.mapper;

import com.sbatis.core.sql.BaseSqlProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;
import java.util.Map;

public interface BaseResultMapper<R> {

	@SelectProvider(type = BaseSqlProvider.class, method = "find")
	R findR(@Param("param") Map<String, Object> param, String tableName);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "find")
	List<R> listR(@Param("param") Map<String, Object> param, String tableName);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "replaceSql")
	R getBySqlR(String sql, @Param("param") List<Object> param);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "replaceSql")
	List<R> listBySqlR(String sql, @Param("param") List<Object> param);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "replaceSql")
	List<Map<String, Object>> listMapBySqlR(String sql, @Param("param") List<Object> param);
	
	@SelectProvider(type = BaseSqlProvider.class, method = "pageSql")
	List<R> pageR(String sql, @Param("param") Map<String, Object> param);
}
