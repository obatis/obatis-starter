package com.obatis.core.constant;

import com.obatis.core.constant.type.JoinTypeEnum;

public class SqlConstant {
	
	private SqlConstant(){}

	/**
	 * 字段属性
	 */
	public static final String BEAN_FIELD = "bean_field";
	/**
	 * 值属性
	 */
	public static final String BEAN_VALUE = "bean_value";
	
	public static final String PROVIDER_OBJ = "provider_obj";
	public static final String PROVIDER_FIELD = "provider_fields";
	public static final String PROVIDER_FILTER = "provider_filters";

	/**
	 * 统计计数的 sql
	 */
	public static final String PROVIDER_COUNT_SQL = "total_sql";
	/**
	 * 查询 sql
	 */
	public static final String PROVIDER_QUERY_SQL = "query_sql";
	/**
	 * 默认起始值
	 */
	public static final int DEFAULT_INIT = 0;

}
