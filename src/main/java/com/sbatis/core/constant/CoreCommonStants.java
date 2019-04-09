package com.sbatis.core.constant;

import java.util.HashMap;
import java.util.Map;

public class CoreCommonStants {
	
	private CoreCommonStants(){};

	/**
	 * 字段属性
	 */
	public static final String BEAN_FIELD = "field";
	/**
	 * 值属性
	 */
	public static final String BEAN_VALUE = "value";
	
	public static final String PARAM_OBJ = "obj";
	public static final String PARAM_FIELD = "fields";
	public static final String PARAM_FILTER = "filters";
	public static final String PARAM_ORDER = "orders";

	/**
	 * 统计计数的 sql
	 */
	public static final String COUNT_SQL = "count_sql";
	/**
	 * 查询 sql
	 */
	public static final String QUERY_SQL = "query_sql";
	
	// 存放class
	public static Map<String, Class<?>> clsCacheMap = new HashMap<>();
	public static final int DEFAULT_INIT = 0;
}
