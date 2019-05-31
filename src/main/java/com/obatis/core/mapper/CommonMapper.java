package com.obatis.core.mapper;

import com.obatis.core.sql.SqlProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;
import java.util.Map;

public interface CommonMapper<R> {

    @SelectProvider(type = SqlProvider.class, method = "find")
    R find(@Param("request") Map<String, Object> param, String tableName);

    @SelectProvider(type = SqlProvider.class, method = "find")
    List<R> list(@Param("request") Map<String, Object> param, String tableName);

    @SelectProvider(type = SqlProvider.class, method = "replaceSql")
    R findBySql(String sql, @Param("request") List<Object> list);

    @SelectProvider(type = SqlProvider.class, method = "replaceSql")
    List<R> listBySql(String sql, @Param("request") List<Object> list);

    @SelectProvider(type = SqlProvider.class, method = "replaceSql")
    List<Map<String, Object>> listMapBySql(String sql, @Param("request") List<Object> list);

    @SelectProvider(type = SqlProvider.class, method = "pageSql")
    List<R> page(String sql, @Param("request") Map<String, Object> params);
}
