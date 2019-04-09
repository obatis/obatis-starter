package com.sbatis.core.constant;

public class SqlConstant {
	
	private SqlConstant(){};

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
	/**
	 * 默认起始值
	 */
	public static final int DEFAULT_INIT = 0;
}
