package com.obatis.core.constant;

import com.obatis.core.mapper.BaseBeanSessionMapper;
import com.obatis.core.mapper.BaseResultSessionMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 缓存静态属性，主要用于存放实体信息、数据库表相关信息
 * @author HuangLongPu
 */
public class CacheInfoConstant {

	private CacheInfoConstant() {}

	/**
	 * 存放表名
	 */
	public static final Map<String, String> TABLE_CACHE = new HashMap<>();
	/**
	 * 存放实体中通过 @Column 注解的属性，key 为实体属性，value为数据库字段
	 */
	public static final Map<String, Map<String, String>> COLUMN_CACHE = new HashMap<>();
	/**
	 * 存放表与实体的映射属性， key为数据库字段，value为实体属性
	 */
	public static final Map<String, Map<String, String>> FIELD_CACHE = new HashMap<>();
	/**
	 * 存放 ResultInfoOutput 的子类的属性
	 */
	public static final Map<String, List<String[]>> RESULT_CACHE = new HashMap<>();

	/**
	 * 存放实体的 sessionMapper
	 */
	public static final Map<String, BaseBeanSessionMapper<?>> BEAN_SESSION_MAPPER = new HashMap<>();
	/**
	 * 存放 ResultInfoOutput 的子类的 sessionMapper
	 */
	public static final Map<String, BaseResultSessionMapper<?>> RESULT_SESSION_MAPPER = new HashMap<>();

	public static final String TABLE_AS_START_PREFIX = "#as_";
}
